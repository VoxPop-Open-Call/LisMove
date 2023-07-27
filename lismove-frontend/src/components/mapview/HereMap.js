import React from "react"

const app_id = 'UH46g5H0bXgWeoG4nnXi';
const app_code = 'ZNT6DEQtIGMGDDe_bhTwiA';
const Here = window.H;

export default class HereMap extends React.Component {
	constructor(props) {
		super(props);

		this.platform = null;
		this.map = null;
		this.router = null;
		this.mapRef = React.createRef();

		this.state = {
			center: props.center,
			zoom: props.zoom,
			theme: props.theme,
			style: props.style,
			markers:[]
		}
	}

	static getDerivedStateFromProps(nextProps, prevState) {
		if(nextProps.markers && prevState.markers && nextProps.markers.length !== prevState.markers.length){
			return {
				markers: nextProps.markers,
				center: nextProps.center,
				route: nextProps.route
			};
		} else if(nextProps.route !== prevState.route) {
			return {
				route:nextProps.route
			};
		}
		else return null;
	}

	getDistance = (lat1, lon1, lat2, lon2)  => {
		let f1 = lat1 * Math.PI / 180, f2 = lat2* Math.PI / 180, dl = (lon2-lon1)* Math.PI / 180, R = 6371e3; // gives d in metres
		let d = Math.acos( Math.sin(f1)*Math.sin(f2) + Math.cos(f1)*Math.cos(f2) * Math.cos(dl) ) * R;
		return d / 1000;
	};

	addMarkers = markers => {
		if(!markers || markers.length === 0) return;

		if(this.markersGroup) this.map.removeObject(this.markersGroup);

		this.markersGroup = new Here.map.Group();
		this.map.addObject(this.markersGroup);

		// add 'tap' event listener, that opens info bubble, to the markersGroup
		this.markersGroup.addEventListener('tap',  (evt) => {
			// event target is the marker itself, markersGroup is a parent event target
			// for all objects that it contains
			if(this.bubble) this.ui.removeBubble(this.bubble);
			this.bubble =  new Here.ui.InfoBubble(evt.target.getPosition(), {
				// read custom data
				content: evt.target.getData()
			});
			this.bubble.addEventListener("statechange", ({target}) => {
				if(target.getState() === 'closed') {
					this.ui.removeBubble(this.bubble);
					this.bubble = null;
				}
			});
			// show info bubble
			this.ui.addBubble(this.bubble);
		}, false);
		markers.forEach( (m) => {

			let coords = {lat: m.lat, lng: m.lng};
			let marker;
			if(m.icon) {
				let icon = new Here.map.Icon(m.icon);
				marker = new Here.map.Marker(coords, {icon});
			} else {
				marker = new Here.map.Marker(coords);
			}
			// Add the marker to the map and center the map at the location of the marker:
			marker.setData(m.data);
			this.markersGroup.addObject(marker);
		});
		this.map.setCenter(this.state.center)
	};

	drawRoute = (points=[]) => {

		let threshold = 1;
		while(points.length > 200) {
			points = points.filter((m,i, data) =>{
				return (i === 0 || this.getDistance(m.lat, m.lng, data[i-1].lat, data[i-1].lng) > threshold)
			});
			threshold *= 1.1;
		}

		let mode = {
			car: 'fastest;car;traffic:disabled',
			pedestrian: 'fastest;pedestrian;traffic:disabled',
		};

		let calculateRouteParams = {
			'mode': mode.car,
			'representation': 'display'
		};

		for(let i = 0; i < points.length; i++) {
			calculateRouteParams["waypoint"+i] = `geo!${points[i].lat},${points[i].lng}`
		}
		let onResult = result => {
			if(result.type === "ApplicationError") {
				if(result.subtype === "NoRouteFound" && calculateRouteParams.mode === mode.car) {
					calculateRouteParams.mode = mode.pedestrian;
					this.router.calculateRoute(calculateRouteParams, onResult, (e) => console.log("pedestrian", e));
				} else {
					console.log(result.details);
				}
			} else {
				if(result.response.route) {
					// Create a linestring to use as a point source for the route line
					let linestring = new Here.geo.LineString();

					result.response.route.forEach(route => {
						// Pick the route's shape:
						let routeShape = route.shape;

						// Push all the points in the shape into the linestring:
						routeShape.forEach(function(point) {
							let parts = point.split(',');
							linestring.pushLatLngAlt(parts[0], parts[1]);
						});
					});

					// Create a polyline to display the route:
					let routeLine = new Here.map.Polyline(linestring, {
						style: { strokeColor: '#AB2430', lineWidth: 8 },
						arrows: { fillColor: 'white', frequency: 2, width: 0.5, length: 0.7 }
					});

					if(this.routeGroup) this.map.removeObject(this.routeGroup);
					this.routeGroup = new Here.map.Group();
					this.routeGroup.addObjects([routeLine]);

					// Add the route polyline and the two markers to the map:
					this.map.addObject(this.routeGroup);

					// Set the map's viewport to make the whole route visible:
					this.map.setViewBounds(routeLine.getBounds());
				}
			}
		};
		let onError = function(error) {
			console.log(error);

		};
		this.router.calculateRoute(calculateRouteParams, onResult, onError);
	};

	clear = () => {
		if(this.routeGroup) {
			this.map.removeObject(this.routeGroup);
			this.routeGroup = null;
		}
		if(this.markersGroup) {
			this.map.removeObject(this.markersGroup);
			this.markersGroup = null;
		}
	};

	componentDidMount() {
		this.platform = new Here.service.Platform({
			app_id,
			app_code,
			useHTTPS: true
		});

		let container = this.mapRef.current;
		let layer = this.platform.createDefaultLayers();

		this.router = this.platform.getRoutingService();

		this.map = new Here.Map(container, layer.normal.map, {
			center: this.state.center,
			zoom: this.state.zoom,
		});
		this.ui = new Here.ui.UI.createDefault(this.map, layer, 'it-IT');
		this.addMarkers(this.props.markers);
		// Enable the event system on the map instance
		let mapEvents = new Here.mapevents.MapEvents(this.map);
		let behavior = new Here.mapevents.Behavior(mapEvents);

	}

	componentDidUpdate() {
		this.clear();
		this.addMarkers(this.props.markers);
		if(this.props.route && this.props.markers.length > 1) {
			this.drawRoute(this.props.markers)
		}
	}

	render() {
		let className, style;
		if(this.props.className) {
			className=this.props.className;
		} else {
			style=this.props.style || {width: '100%', height: '400px', background: 'grey' }
		}

		return (
			<div style={style} className={className} ref={this.mapRef}/>
		);
	}
}
