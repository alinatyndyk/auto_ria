import { faCar, faInfoCircle, faLocationArrow, faUser } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import './Footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-section about">
                    <h3><FontAwesomeIcon icon={faInfoCircle} /> About Us</h3>
                    <p>We offer a wide selection of cars to suit every taste and budget. Our goal is to help you find the perfect car quickly and conveniently.</p>
                </div>
                <div className="footer-section links">
                    <h3><FontAwesomeIcon icon={faLocationArrow} /> Useful Links</h3>
                    <ul>
                        <li><a href="/about">About the Company</a></li>
                        <li><a href="/contact">Contact Us</a></li>
                        <li><a href="/faq">FAQ</a></li>
                        <li><a href="/terms">Terms of Service</a></li>
                        <li><a href="/privacy">Privacy Policy</a></li>
                    </ul>
                </div>
                <div className="footer-section contact">
                    <h3><FontAwesomeIcon icon={faUser} /> Contact Information</h3>
                    <ul>
                        <li>Email: info@carsales.com</li>
                        <li>Phone: +1 (800) 123-4567</li>
                        <li>Address: 123 Example St, City, Country</li>
                    </ul>
                </div>
                <div className="footer-section social">
                    <h3><FontAwesomeIcon icon={faCar} /> Follow Us on Social Media</h3>
                    <div className="social-links">
                        <a href="https://facebook.com"><FontAwesomeIcon icon={faCar} /></a>
                        <a href="https://twitter.com"><FontAwesomeIcon icon={faCar} /></a>
                        <a href="https://instagram.com"><FontAwesomeIcon icon={faCar} /></a>
                        <a href="https://linkedin.com"><FontAwesomeIcon icon={faCar} /></a>
                    </div>
                </div>
            </div>
            <div className="footer-bottom">
                <p>&copy; 2024 CarSales. All rights reserved.</p>
            </div>
        </footer>
    );
};

export default Footer;

