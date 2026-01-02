import { User, Calendar, CreditCard, MessageCircle, ChevronRight, LogOut } from 'lucide-react';

export function Profile() {
  const member = {
    name: 'Alex Martinez',
    email: 'alex.martinez@email.com',
    phone: '+91 98765 43210',
    memberSince: '2024-08-15',
    batch: 'Adult Batch - Evening',
  };

  const membershipHistory = [
    {
      id: '1',
      plan: 'Monthly Unlimited',
      startDate: '2024-12-15',
      endDate: '2025-01-15',
      status: 'active',
    },
    {
      id: '2',
      plan: 'Quarterly Unlimited',
      startDate: '2024-08-15',
      endDate: '2024-11-15',
      status: 'completed',
    },
  ];

  const paymentHistory = [
    {
      id: '1',
      amount: '₹2,500',
      date: '2024-12-15',
      method: 'UPI',
      status: 'Completed',
    },
    {
      id: '2',
      amount: '₹6,500',
      date: '2024-08-15',
      method: 'UPI',
      status: 'Completed',
    },
  ];

  return (
    <div className="px-6 py-8 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl mb-2">Profile</h1>
        <p className="text-neutral-400">Your account details</p>
      </div>

      {/* Member Info Card */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 bg-gradient-to-br from-amber-600 to-amber-700 rounded-full flex items-center justify-center">
            <User className="w-8 h-8 text-neutral-950" />
          </div>
          <div>
            <div className="text-xl mb-1">{member.name}</div>
            <div className="text-sm text-neutral-400">Member since {new Date(member.memberSince).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })}</div>
          </div>
        </div>
        
        <div className="space-y-3 pt-4 border-t border-neutral-800">
          <div className="flex justify-between items-center">
            <span className="text-sm text-neutral-400">Email</span>
            <span className="text-sm">{member.email}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-sm text-neutral-400">Phone</span>
            <span className="text-sm">{member.phone}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-sm text-neutral-400">Batch</span>
            <span className="text-sm">{member.batch}</span>
          </div>
        </div>
      </div>

      {/* Membership History */}
      <div>
        <h2 className="text-xl mb-4 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-neutral-400" />
          Membership History
        </h2>
        <div className="space-y-3">
          {membershipHistory.map((membership) => (
            <div
              key={membership.id}
              className="bg-neutral-900 border border-neutral-800 rounded-xl p-4"
            >
              <div className="flex items-start justify-between mb-2">
                <div>
                  <div className="text-sm mb-1">{membership.plan}</div>
                  <div className="text-xs text-neutral-400">
                    {new Date(membership.startDate).toLocaleDateString('en-US', { 
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric'
                    })} - {new Date(membership.endDate).toLocaleDateString('en-US', { 
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric'
                    })}
                  </div>
                </div>
                <span
                  className={`text-xs px-2 py-1 rounded ${
                    membership.status === 'active'
                      ? 'bg-emerald-950/40 text-emerald-500'
                      : 'bg-neutral-800 text-neutral-400'
                  }`}
                >
                  {membership.status === 'active' ? 'Active' : 'Completed'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Payment History */}
      <div>
        <h2 className="text-xl mb-4 flex items-center gap-2">
          <CreditCard className="w-5 h-5 text-neutral-400" />
          Payment History
        </h2>
        <div className="space-y-3">
          {paymentHistory.map((payment) => (
            <div
              key={payment.id}
              className="bg-neutral-900 border border-neutral-800 rounded-xl p-4"
            >
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm mb-1">{payment.amount}</div>
                  <div className="text-xs text-neutral-400">
                    {new Date(payment.date).toLocaleDateString('en-US', { 
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric'
                    })} • {payment.method}
                  </div>
                </div>
                <span className="text-xs text-emerald-500">
                  {payment.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Support */}
      <div>
        <h2 className="text-xl mb-4">Support</h2>
        <button className="w-full bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-neutral-50 py-4 rounded-xl transition-colors flex items-center justify-between px-6">
          <div className="flex items-center gap-3">
            <MessageCircle className="w-5 h-5 text-amber-500" />
            <span>Contact Gym Support</span>
          </div>
          <ChevronRight className="w-5 h-5 text-neutral-500" />
        </button>
      </div>

      {/* Logout */}
      <button className="w-full bg-neutral-900 hover:bg-neutral-800 border border-neutral-800 text-red-500 py-4 rounded-xl transition-colors flex items-center justify-center gap-2">
        <LogOut className="w-5 h-5" />
        <span>Sign Out</span>
      </button>
    </div>
  );
}
