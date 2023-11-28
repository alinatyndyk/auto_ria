import React, {useState} from 'react';

export type Image = {
    id: number,
    src: string
};

const Carousel: React.FC<{ images: Image[] }> = ({images}) => {
    const [currentIndex, setCurrentIndex] = useState(0);

    // console.log(images, "images");
    // console.log(images[0], "images");
    // console.log(images[1], "images1");
    // console.log(images[2], "images2");

    const goToPrevious = () => {
        setCurrentIndex(currentIndex === 0 ? images.length - 1 : currentIndex - 1);
    }

    const goToNext = () => {
        setCurrentIndex(currentIndex === images.length - 1 ? 0 : currentIndex + 1);
    }

    return (
        <div style={{display: "flex"}} className="carousel">
            <div className="carousel-item">
                <img height={"80px"} src={`${images[currentIndex === 0 ? images.length - 1 : currentIndex - 1].src}`} alt="Previous"
                     className="previous-image"/>
            </div>
            <div className="carousel-item">
                <img height={"80px"} src={`${images[0].src}`} alt="Current" className="current-image"/>
            </div>
            <div className="carousel-item">
                <img height={"80px"} src={`${images[currentIndex === images.length - 1 ? 0 : currentIndex + 1].src}`} alt="Next"
                     className="next-image"/>
            </div>
            <button onClick={goToPrevious} className="carousel-button previous-button">
                Previous
            </button>
            <button onClick={goToNext} className="carousel-button next-button">
                Next
            </button>
        </div>
    );
}
export {Carousel};