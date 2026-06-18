export type Step = 1 | 2 | 3;

export interface StandardError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface GroupData {
  externalId: string,
  name: string,
  description: string,
  inviteCode: string,
  createdAt: Date,
  eventDate: Date,
  members: {
    externalId: string,
    displayName: string,
    username: string,
    role: string,
    email: string
  }[],
  eventStops?: {
    name: string,
    latitude: number,
    longitude: number,
    category: string,
    stopOrder: number,
    city?: string,
    state?: string
  }[]
}

export interface GroupSimpleData {
  externalId: string,
  name: string,
  description: string,
  inviteCode: string,
  eventDate: Date,
  memberAmount: number
}

export interface EventFormData {
  name: string;
  date: string;
  description: string;
}

export interface EventStop {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  category?: string;
  time?: string;
}

export interface PlaceSuggestion {
  placePrediction: {
    placeId: string;
    text: {
      text: string;
    };
    structuredFormat: {
      mainText: {
        text: string;
      };
      secondaryText?: {
        text: string;
      };
    };
    types?: string[];
  };
}

export interface PlaceDetails {
  id: string;
  displayName: {
    text: string;
  };
  location: {
    latitude: number;
    longitude: number;
  };
  primaryTypeDisplayName?: {
    text: string;
  };
}

export interface FriendData {
  fsExternalId: string;
  friendExternalId: string;
  friendUsername: string;
  friendDisplayName: string;
  status: string;
}

export interface FriendshipData {
  fsExternalId: string;
  requesterExternalId: string;
  receiverExternalId: string;
  requesterUsername: string;
  receiverUsername: string;
  friendshipDate: Date;
  status: string;
}

export interface UserData {
  externalId: string;
  username: string;
  displayName: string;
  email: string;
  birthDate: Date;
}

export interface ExpenseContribution {
  expenseContributionExternalId: string;
  value: number;
  payerExternalId: string;
  parentExpenseExternalId: string;
}

export interface ExpenseDistribution {
  expenseDistributionExternalId: string;
  value: number;
  status: string;
  debtorExternalId: string;
  creditorExternalId: string;
  parentExpenseExternalId: string;
}

export interface ExpenseData {
  expenseExternalId: string;
  description: string;
  totalValue: number;
  contributions: ExpenseContribution[];
  distributions: ExpenseDistribution[];
}
