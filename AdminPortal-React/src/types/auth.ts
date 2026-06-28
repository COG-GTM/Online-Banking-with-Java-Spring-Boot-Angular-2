export interface Credentials {
  username: string;
  password: string;
}

export interface AuthContextValue {
  loggedIn: boolean;
  /** Sends credentials to the backend; resolves on success, throws on failure. */
  login: (
    username: string,
    password: string,
    signal?: AbortSignal,
  ) => Promise<void>;
  /** Logs out on the backend and clears local session state. */
  logout: (signal?: AbortSignal) => Promise<void>;
}
