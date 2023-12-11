import React, {useState} from 'react';

export type Image = {
    id: number,
    src: string
};

const Carousel: React.FC<{ images: Image[] }> = ({images}) => {
    const [currentIndex, setCurrentIndex] = useState(0);

    const goToPrevious = () => {
        setCurrentIndex((prevIndex) => (prevIndex === 0 ? images.length - 1 : prevIndex - 1));
    };

    const goToNext = () => {
        setCurrentIndex((prevIndex) => (prevIndex === images.length - 1 ? 0 : prevIndex + 1));
    };

    return (
        <div style={{display: "flex"}} className="carousel">
            <div className="carousel-item">
                <img height={"200px"} src={images[currentIndex === 0 ? images.length - 1 : currentIndex - 1].src}
                     alt="Previous" className="previous-image"/>
            </div>
            <div className="carousel-item">
                <img height={"250px"} src={images[currentIndex].src} alt="Current" className="current-image"/>
                <div>

                    <button onClick={goToPrevious} className="carousel-button previous-button">
                        Previous
                    </button>
                    <button onClick={goToNext} className="carousel-button next-button">
                        Next
                    </button>
                </div>
            </div>
            <div className="carousel-item">
                <img height={"200px"} src={images[currentIndex === images.length - 1 ? 0 : currentIndex + 1].src}
                     alt="Next" className="next-image"/>
            </div>
        </div>
    );
};
export {Carousel};