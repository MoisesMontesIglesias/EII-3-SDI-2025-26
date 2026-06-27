import { useState } from "react";
import "../assets/Login.css";
import { login } from "../services/ITunesService";

const Login = ({ onLogin }) => {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const isFormValid = email.trim().length > 0 && password.trim().length > 0;

    const handleLogin = async (e) => {
        e.preventDefault();

        if (!isFormValid) {
            setError("Introduce email y password");
            return;
        }

        setLoading(true);
        setError("");

        try {
            const data = await login(email, password);

            if (!data.token) {
                throw new Error(data.message || "Login invalido");
            }

            localStorage.setItem("token", data.token);
            onLogin();
        } catch (err) {
            localStorage.removeItem("token");
            setError(err.message || "No se ha podido iniciar sesion");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <h2>Iniciar sesion</h2>

            <form onSubmit={handleLogin} className="login-form">
                <input
                    type="email"
                    placeholder="email@email.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />

                <input
                    type="password"
                    placeholder="contraseña"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />

                {error && <div className="error">{error}</div>}

                <button type="submit" disabled={!isFormValid || loading}>
                    {loading ? "Entrando..." : "Aceptar"}
                </button>
            </form>
        </div>
    );
};

export default Login;
