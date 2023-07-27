import React, {Component} from "react";
import {
	Container,
	Row,
	Col,
	CardGroup,
	Card,
	CardBody
} from "reactstrap";

export class Placeholder extends Component {

	render() {
		return (
			<Container>
				<Row className="justify-content-center">
					<Col md="8">
						<CardGroup className="mb-0">
							<Card className="p-4">
								<CardBody className="card-body">
									<img style={{
										width: "50%", display: "block",
										marginLeft: "auto",
										marginRight: "auto"
									}} src="/img/logo.png"/>
									<p className="h2 text-center">Work in progress</p>
								</CardBody>
							</Card>
						</CardGroup>
					</Col>
				</Row>
			</Container>
		);
	}
}

export default Placeholder;
