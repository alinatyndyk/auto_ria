import {TypedUseSelectorHook, useDispatch, useSelector} from "react-redux";
import {AppDispatch, RootState} from "../redux";
import {useNavigate, useParams} from "react-router";

const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
const useAppDispatch = () => useDispatch<AppDispatch>();
const useAppNavigate = () => useNavigate();
export {
    useAppDispatch,
    useAppNavigate,
    useAppSelector
}