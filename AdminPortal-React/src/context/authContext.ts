import { createContext } from 'react';
import type { AuthContextValue } from '../types/auth';

/** Key used to persist the admin session flag, matching the Angular app. */
export const AUTH_STORAGE_KEY = 'PortalAdminHasLoggedIn';

export const AuthContext = createContext<AuthContextValue | undefined>(
  undefined,
);
