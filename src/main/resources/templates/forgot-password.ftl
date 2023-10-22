<html lang="">
<head>
    <style>
        h2 {
            color: #007bff;
        }
    </style>
</head>
<body>
<h2>Welcome to our website!</h2>
<p>Greetings from Autoria Team!</p>
<p>Your account with email ${email} requested a password reset at ${time}</p>
<p>If this was you, please press the on link below. Otherwise, ignore this letter</p>
<a href="http://localhost:3000/auth/forgot-password?code=${code}">Change password</a>
</body>
</html>