import { useMutation, useQueryClient } from '@tanstack/react-query';
import { authApi } from '../api/auth.api';
import { useAuthStore } from '../store/auth.store';
import { LoginRequest, AuthResponse } from '../types/auth.types';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

export const useLogin = () => {
  const queryClient = useQueryClient();
  const login = useAuthStore((state) => state.login);
  const navigate = useNavigate();

  return useMutation<AuthResponse, Error, LoginRequest>({
    mutationFn: (data) => authApi.login(data),
    onSuccess: (response) => {
      login(response);
      queryClient.invalidateQueries();
      toast.success('Login realizado com sucesso!');
      navigate('/dashboard');
    },
    onError: (error) => {
      const message = error.message || 'Erro ao fazer login';
      toast.error(message);
    },
  });
};