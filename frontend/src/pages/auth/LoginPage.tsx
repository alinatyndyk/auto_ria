import { FC } from 'react';
import { LoginForm } from "../../forms";

const LoginPage: FC = () => {

    // if (localStorage.getItem("isAuth") != undefined && localStorage.getItem("isAuth") === "true") {
        return (
        <div>
            <LoginForm />
        </div>
    );
// } else {
//         <div>
//             The login page couldn't be accessed
//         </div>
//     }
};

export { LoginPage };

