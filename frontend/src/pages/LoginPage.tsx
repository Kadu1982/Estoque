import React from 'react';
import { AuthLayout } from '../components/layout/AuthLayout';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useLogin } from '../hooks/useAuth';

const loginSchema = z.object({
  login: z.string().min(1, 'Usuário ou e-mail é obrigatório'),
  password: z.string().min(1, 'Senha é obrigatória'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  document.title = 'Login - Austral Estoque';

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const loginMutation = useLogin();

  const onSubmit = (data: LoginFormData) => {
    loginMutation.mutate(data);
  };

  return (
    <AuthLayout>
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="text-center mb-4">
          <h2 className="text-xl font-semibold text-text">Entrar</h2>
          <p className="text-sm text-text-muted">Digite suas credenciais para acessar</p>
        </div>

        <Input
          label="Usuário ou e-mail"
          {...register('login')}
          error={errors.login?.message}
          autoComplete="username"
        />

        <Input
          label="Senha"
          type="password"
          {...register('password')}
          error={errors.password?.message}
          autoComplete="current-password"
        />

        <Button
          type="submit"
          className="w-full"
          isLoading={loginMutation.isPending}
        >
          Entrar
        </Button>
      </form>
    </AuthLayout>
  );
};