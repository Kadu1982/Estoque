declare module 'zustand/middleware' {
  import { StateCreator } from 'zustand';
  
  export function persist<T>(
    f: StateCreator<T>,
    options: { name: string }
  ): StateCreator<T>;
}
