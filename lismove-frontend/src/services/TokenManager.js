import Cookies from 'universal-cookie';

const cookies = new Cookies();

export class TokenManager {

	constructor() {
		this.storage = localStorage;
		this.tokenName = "JW-NXT";
	}

	save(token) {
		//this.storage.setItem(this.tokenName, token);
		cookies.remove(this.tokenName);
		cookies.set(this.tokenName, token);
	}

	get() {
		//return this.storage.getItem(this.tokenName);
		return cookies.get(this.tokenName);
	}

	delete() {
		//this.storage.removeItem(this.tokenName);
		cookies.remove(this.tokenName);
	}
}
