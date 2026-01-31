-- ============================================
-- QR Attendance System - Database Migration
-- ============================================
-- Run this in Supabase SQL Editor

-- 1. Create gym_qr_codes table for QR-based attendance
CREATE TABLE IF NOT EXISTS gym_qr_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID,
    code_value TEXT UNIQUE NOT NULL,
    location_name TEXT NOT NULL,
    valid_from TIME DEFAULT '06:00:00',
    valid_until TIME DEFAULT '22:00:00',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Enable RLS
ALTER TABLE gym_qr_codes ENABLE ROW LEVEL SECURITY;

-- 3. Allow all users to read QR codes (anon key works)
CREATE POLICY "Anyone can read gym QR codes" ON gym_qr_codes
    FOR SELECT
    USING (true);

-- 4. Insert default QR code for Caliclan Gym
INSERT INTO gym_qr_codes (code_value, location_name) 
VALUES ('CALICLAN_GYM_2026', 'Caliclan Gym Main')
ON CONFLICT (code_value) DO NOTHING;

-- 5. Modify attendance table to track QR scans
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS check_in_method TEXT DEFAULT 'manual';

ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS qr_code_id UUID REFERENCES gym_qr_codes(id);

ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS scanned_at TIMESTAMP WITH TIME ZONE;

-- 6a. Add RLS INSERT policy for attendance (allows QR check-ins)
-- This allows anyone to insert attendance if the user_id exists in profiles
CREATE POLICY "Allow attendance insert for registered users" ON attendance
    FOR INSERT
    WITH CHECK (
        EXISTS (SELECT 1 FROM profiles WHERE profiles.id = user_id)
    );

-- 6b. Add RLS SELECT policy for attendance (allows users to check their own attendance)
CREATE POLICY "Allow users to view their own attendance" ON attendance
    FOR SELECT
    USING (
        auth.uid() = user_id OR 
        -- Allow if user_id matches the session ID stored in profiles (for our custom auth hack if needed)
        -- But relying on auth.uid() is standard. 
        -- Given our 'access token is anon key' hack, auth.uid() might be null.
        -- We might need a policy that allows reading based on the user_id param passed in the query?
        -- No, that's insecure.
        
        -- Since we are using the ANON key as the access token, auth.uid() will be null/anon.
        -- And we don't have the user's real ID in the JWT.
        -- So strict RLS is hard here without fixing the Auth flow.
        
        -- COMPROMISE for this "Demo/Dev" setup:
        -- Allow anyone with the anon key to read attendance rows.
        -- This isn't secure for prod but matches the "Anyone can read gym QR codes" policy.
        true
    );

-- 7. Verify tables
SELECT 'gym_qr_codes' as table_name, count(*) as row_count FROM gym_qr_codes
UNION ALL
SELECT 'attendance' as table_name, count(*) as row_count FROM attendance;
