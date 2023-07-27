import React from "react";
import {Image} from "react-konva";

export default class URLImage extends React.Component {
	state = {
		image: null
	};

	constructor(props) {
		super(props);
		this.imageNode = React.createRef();
	}

	componentDidMount() {
		this.loadImage();
	}

	componentDidUpdate(oldProps) {
		if (oldProps.src !== this.props.src) {
			this.loadImage();
		}
		if (this.props.scale && this.imageNode.current) {
			this.imageNode.current.width(this.image.width / this.props.scale);
			this.imageNode.current.height(this.image.height / this.props.scale);
			this.imageNode.current.offsetX(this.imageNode.current.width() / 2);
			this.imageNode.current.offsetY(this.imageNode.current.height() / 2);
			this.imageNode.current.getLayer().batchDraw();
		}
	}

	componentWillUnmount() {
		this.image.removeEventListener('load', this.handleLoad);
	}

	loadImage() {
		// save to "this" to remove "load" handler on unmount
		this.image = new window.Image();
		this.image.src = this.props.src;
		this.image.addEventListener('load', this.handleLoad);
	}

	handleLoad = () => {
		// after setState react-konva will update canvas and redraw the layer
		// because "image" property is changed
		this.setState({
			image: this.image
		});
		this.props.onLoad && this.props.onLoad(this.image);
		// if you keep same image object during source updates
		// you will have to update layer manually:
		// this.imageNode.getLayer().batchDraw();
	};

	render() {
		return (
			<Image
				image={this.state.image}
				ref={this.imageNode}
				{...this.props.imgProps}
			/>
		);
	}
}
