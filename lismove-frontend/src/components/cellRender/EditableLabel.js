import React, {useState} from 'react';
import makeStyles from '@mui/styles/makeStyles';
import HoverableButton from "../layout/HoverableButton";
import EditFieldModal from "../modals/EditFieldModal";
import EditIcon from '@mui/icons-material/Edit';

export default function EditableLabel({value, displayValue, onChange, title, type, required, min, max, step, measureUnit, options, infoMessage, warningMessage}) {
	let classes = useStyles();
	let [modal, setModal] = useState(false);

	return <div>
		<HoverableButton
			item={{
				icon: <EditIcon/>,
				name: displayValue || ""
			}}
			onClick={() => setModal(true)}
			classes={{root: classes.root}}
		/>
		<EditFieldModal open={modal} onClose={() => setModal(false)} defaultValue={value} onSubmit={onChange} title={title}
						type={type} required={required} min={min} max={max} step={step} measureUnit={measureUnit} options={options}
						infoMessage={infoMessage} warningMessage={warningMessage}/>
	</div>
}

const useStyles = makeStyles(theme => ({
	root: {
		color: theme.palette.primary.main,
		'&:hover': {
			color: theme.palette.secondary.main
		}
	}
}));
