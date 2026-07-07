export type Step = 1 | 2 | 3;

export interface StandardError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

/* -------------------------------------------------------------------------- */
/*  Grupos (Repúblicas / Casas)                                               */
/*  Alinhado ao GroupResponse do backend roomiesapp:                          */
/*  id, externalId, name, description, inviteCode, address,                   */
/*  monthlyRentCents, maxOccupants, createdAt                                 */
/* -------------------------------------------------------------------------- */

export type GroupRole = "ADMIN" | "MEMBER";

/**
 * Morador de uma casa. Alinhado ao UserResponse do backend
 * (id numérico como string, username, name) + o papel no grupo.
 * `members` só vem populado se o backend incluir a lista no GroupResponse.
 */
export interface RoomieMember {
  id: string;
  username: string;
  name?: string;
  email?: string;
  role?: GroupRole;
}

export interface GroupData {
  id: number;
  externalId: string;
  name: string;
  description: string;
  inviteCode: string;
  address?: string | null;
  monthlyRentCents?: number | null;
  maxOccupants?: number;
  createdAt: string;
  members?: RoomieMember[];
}

export interface GroupSimpleData {
  id: number;
  externalId: string;
  name: string;
  description: string;
  inviteCode: string;
  address?: string | null;
  monthlyRentCents?: number | null;
  maxOccupants?: number;
  createdAt: string;
  memberAmount?: number;
}

/* -------------------------------------------------------------------------- */
/*  Amigos (mantido para a página de amigos — depende de backend próprio)     */
/* -------------------------------------------------------------------------- */

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

/* -------------------------------------------------------------------------- */
/*  Financeiro (módulo billing do roomiesapp)                                 */
/*  Alinhado a BillResponse / DebtResponse / PixCodeResponse                  */
/* -------------------------------------------------------------------------- */

export type BillType = "NORMAL" | "RECURRING";

export type RecurrenceInterval = "NONE" | "MONTHLY" | "ANNUAL" | "CUSTOM";

export type DebtStatus =
  | "PENDING"
  | "AWAITING_CONFIRMATION"
  | "PAID"
  | "CANCELLED";

export interface BillUser {
  id: string;
  username: string;
  name?: string;
  email?: string;
  phoneNumber?: string;
}

export interface BillResponse {
  externalId: string;
  groupId: string;
  title: string;
  description?: string | null;
  totalCents: number;
  billType: BillType;
  recurrenceInterval?: RecurrenceInterval | null;
  customIntervalDays?: number | null;
  dueDate?: string | null;
  contributor?: BillUser | null;
  participants: BillUser[];
}

export interface DebtResponse {
  externalId: string;
  billId: string;
  debtor?: BillUser | null;
  creditor?: BillUser | null;
  amountInCents: number;
  status: DebtStatus;
  pixCopiaECola?: string | null;
}

export interface PixCodeResponse {
  debtId: string;
  pixCopiaECola: string;
  recipientName?: string;
  recipientCity?: string;
  amountInCents: number;
}

/* -------------------------------------------------------------------------- */
/*  Household (tarefas, contas de casa, avisos)                               */
/* -------------------------------------------------------------------------- */

export interface HouseBill {
  id: string;
  description: string;
  totalValue: number;
  dueDate: string;
  billType: 'RENT' | 'ELECTRICITY' | 'WATER' | 'INTERNET' | 'OTHER';
  status: 'PENDING' | 'PAID';
}

export interface Chore {
  id: string;
  title: string;
  assignedTo?: {
    externalId: string;
    displayName: string;
  };
  dueDate: string;
  isCompleted: boolean;
  points: number;
}

export interface HouseNotice {
  id: string;
  title: string;
  content: string;
  createdAt: string;
}
