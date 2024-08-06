import { useContext } from "react";
import { ThemeContext } from "../../Context";

const useTheme = () => {
  return useContext(ThemeContext);
};

export default useTheme;