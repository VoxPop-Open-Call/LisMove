import React, {Component} from "react";
import Grid from "@mui/material/Grid";
import Box from "@mui/material/Box";
import {Link} from "react-router-dom";

class Page404 extends Component {
  render() {
    return (
      <Grid container>
        <Grid item xs={12}>
          <Box fontSize="h1.fontSize" textAlign="center">404</Box>
        </Grid>
        <Grid item xs={12}>
          <Box fontSize="h5.fontSize" textAlign="center">
            Torna alla <Link to={"/"}>Home</Link>
          </Box>
        </Grid>
      </Grid>
    );
  }
}

export default Page404;
