import { Check, ArrowRight } from 'lucide-react';

interface MembershipPlan {
  id: string;
  name: string;
  duration: string;
  price: string;
  features: string[];
  recommended?: boolean;
  current?: boolean;
}

export function Membership() {
  const plans: MembershipPlan[] = [
    {
      id: '1',
      name: 'Monthly Unlimited',
      duration: '1 Month',
      price: '₹2,500',
      features: [
        'Unlimited access to all sessions',
        'Weekly structured training program',
        'Community support',
        'Coach guidance',
      ],
      current: true,
    },
    {
      id: '2',
      name: 'Quarterly Unlimited',
      duration: '3 Months',
      price: '₹6,500',
      features: [
        'Unlimited access to all sessions',
        'Weekly structured training program',
        'Community support',
        'Coach guidance',
        'Save 13% vs monthly',
      ],
      recommended: true,
    },
    {
      id: '3',
      name: 'Annual Unlimited',
      duration: '12 Months',
      price: '₹24,000',
      features: [
        'Unlimited access to all sessions',
        'Weekly structured training program',
        'Community support',
        'Coach guidance',
        'Priority workshop access',
        'Save 20% vs monthly',
      ],
    },
  ];

  const currentPlan = plans.find((p) => p.current);

  return (
    <div className="px-6 py-8 space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl mb-2">Membership</h1>
        <p className="text-neutral-400">Manage your gym access</p>
      </div>

      {/* Current Membership */}
      {currentPlan && (
        <div className="bg-gradient-to-br from-amber-950/40 to-neutral-900 border-2 border-amber-600 rounded-2xl p-6">
          <div className="text-xs uppercase tracking-wider text-amber-500 mb-2">
            Current Plan
          </div>
          <div className="flex items-start justify-between mb-4">
            <div>
              <div className="text-2xl mb-1">{currentPlan.name}</div>
              <div className="text-neutral-400">{currentPlan.duration}</div>
            </div>
            <div className="text-right">
              <div className="text-2xl">{currentPlan.price}</div>
              <div className="text-sm text-neutral-400">per month</div>
            </div>
          </div>
          <div className="space-y-2 mb-6">
            {currentPlan.features.map((feature) => (
              <div key={feature} className="flex items-start gap-2 text-sm text-neutral-300">
                <Check className="w-4 h-4 text-amber-500 mt-0.5 flex-shrink-0" />
                <span>{feature}</span>
              </div>
            ))}
          </div>
          <div className="pt-4 border-t border-neutral-700">
            <div className="text-sm text-neutral-400 mb-1">Next billing date</div>
            <div>February 15, 2025</div>
          </div>
        </div>
      )}

      {/* Available Plans */}
      <div>
        <h2 className="text-xl mb-4">Upgrade or Renew</h2>
        <div className="space-y-4">
          {plans
            .filter((plan) => !plan.current)
            .map((plan) => (
              <div
                key={plan.id}
                className={`rounded-2xl p-6 border-2 ${
                  plan.recommended
                    ? 'bg-neutral-900 border-amber-700'
                    : 'bg-neutral-900 border-neutral-800'
                }`}
              >
                {plan.recommended && (
                  <div className="text-xs uppercase tracking-wider text-amber-500 mb-3">
                    Recommended
                  </div>
                )}
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <div className="text-xl mb-1">{plan.name}</div>
                    <div className="text-sm text-neutral-400">{plan.duration}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-xl">{plan.price}</div>
                    <div className="text-xs text-neutral-400">total</div>
                  </div>
                </div>
                <div className="space-y-2 mb-5">
                  {plan.features.map((feature) => (
                    <div key={feature} className="flex items-start gap-2 text-sm text-neutral-400">
                      <Check className="w-4 h-4 text-neutral-500 mt-0.5 flex-shrink-0" />
                      <span>{feature}</span>
                    </div>
                  ))}
                </div>
                <button
                  className={`w-full py-3 rounded-xl transition-colors flex items-center justify-center gap-2 ${
                    plan.recommended
                      ? 'bg-amber-600 hover:bg-amber-700 text-neutral-950'
                      : 'bg-neutral-800 hover:bg-neutral-700 text-neutral-50'
                  }`}
                >
                  <span>Select Plan</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            ))}
        </div>
      </div>

      {/* Contact Note */}
      <div className="bg-neutral-900 border border-neutral-800 rounded-2xl p-6">
        <div className="text-sm text-neutral-400 leading-relaxed">
          Need a custom plan or have questions? Contact us via WhatsApp for personalized membership options.
        </div>
      </div>
    </div>
  );
}
