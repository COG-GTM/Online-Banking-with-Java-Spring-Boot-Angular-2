import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../../auth/useAuth'
import { homePathForUser } from '../../auth/roles'
import { ApiError } from '../../lib/apiClient'
import { Loading } from '../../components/Loading'

const loginSchema = z.object({
  username: z.string().min(1, 'Username is required'),
  password: z.string().min(1, 'Password is required'),
})

type LoginForm = z.infer<typeof loginSchema>

export function LoginPage() {
  const { user, status, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [formError, setFormError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginForm>({
    defaultValues: { username: '', password: '' },
  })

  if (status === 'loading') {
    return <Loading />
  }

  // Already signed in — mirror the legacy behavior of not re-showing the login page.
  if (user) {
    return <Navigate to={homePathForUser(user)} replace />
  }

  const onSubmit = handleSubmit(async (values) => {
    const parsed = loginSchema.safeParse(values)
    if (!parsed.success) {
      setFormError('Please fill in username and password.')
      return
    }
    setFormError(null)
    try {
      const me = await login(parsed.data)
      const from = (location.state as { from?: Location } | null)?.from
      navigate(from?.pathname ?? homePathForUser(me), { replace: true })
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        setFormError('Invalid username or password')
      } else {
        setFormError('Something went wrong. Please try again.')
      }
    }
  })

  return (
    <div className="container">
      <div className="row justify-content-center mt-5">
        <div className="col-md-5">
          <div className="card shadow-sm">
            <div className="card-body">
              <h3 className="card-title text-center mb-4">Online Banking</h3>

              {formError && (
                <div className="alert alert-danger" role="alert">
                  {formError}
                </div>
              )}

              <form onSubmit={onSubmit} noValidate>
                <div className="mb-3">
                  <label htmlFor="username" className="form-label">
                    Username
                  </label>
                  <input
                    id="username"
                    type="text"
                    autoComplete="username"
                    className={`form-control ${errors.username ? 'is-invalid' : ''}`}
                    {...register('username', { required: 'Username is required' })}
                  />
                  {errors.username && (
                    <div className="invalid-feedback">{errors.username.message}</div>
                  )}
                </div>

                <div className="mb-3">
                  <label htmlFor="password" className="form-label">
                    Password
                  </label>
                  <input
                    id="password"
                    type="password"
                    autoComplete="current-password"
                    className={`form-control ${errors.password ? 'is-invalid' : ''}`}
                    {...register('password', { required: 'Password is required' })}
                  />
                  {errors.password && (
                    <div className="invalid-feedback">{errors.password.message}</div>
                  )}
                </div>

                <button
                  type="submit"
                  className="btn btn-primary w-100"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Signing in…' : 'Login'}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
