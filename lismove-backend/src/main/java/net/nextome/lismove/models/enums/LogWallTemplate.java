package net.nextome.lismove.models.enums;

public enum LogWallTemplate {
	/*
TAG AMMESSI
<a href="...">
<b>
<big>
<blockquote>
<br>
<cite>
<dfn>
<div align="...">
<em>
<font size="..." color="..." face="...">
<h1>
<h2>
<h3>
<h4>
<h5>
<h6>
<i>
<img src="...">
<p>
<small>
<strike>
<strong>
<sub>
<sup>
<tt>
<u>
 */
	/*
	[nickname] del progetto [nome progetto] ha registrato una sessione [aggiungere se casa/lavoro]  in [indicare il mezzo] di [lunghezza] guadagnando [n € - in grassetto] e [n punti urbani] e [n punti nazionali]
[nickname] del progetto [nome progetto] ha vinto il premio di [n € - in grassetto o pacchetto di punti urbani] nella classifica [mensile?]
[nickname] del progetto [nome progetto] ha vinto la coppa [nome coppa] pari ad [n € in grassetto]
[nickname] del progetto [nome progetto] ha riscattato il voucher di [n € in grassetto] nell’attività [nome negozio, link a scheda negozio se si clicca]
[nickname] ha utilizzato [n punti urbani] per il premio offerto dall’attività [nome negozio, link a scheda negozio se si clicca]
	 */

	SESSION("<li><b><u>%s</u></b><br/>%s ha registrato una sessione in %s di <b>%s km</b> guadagnando <b><font color='#FF0000'>%s punti nazionali</font></b></li>"),
	SESSION_INIZIATIVE("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha registrato una sessione in %s di <b>%s km</b> guadagnando <b><font color='#FF0000'>%s punti iniziativa</font></b> e <b><font color='#FF0000'>%s punti nazionali</font></b></li>"),
	SESSION_INIZIATIVE_EURO("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha registrato una sessione in %s di <b>%skm </b> guadagnando <b><font color='#6BA32D'>%s €</font></b> e <b><font color='#FF0000'>%s punti iniziativa</font></b> e <b><font color='#FF0000'>%s punti nazionali</font></b></li>"),
	SESSION_INIZIATIVE_HOMEWORK("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha registrato una sessione casa/lavoro in %s di <b>%s km</b> guadagnando <b><font color='#FF0000'>%s punti iniziativa</font></b> e <b><font color='#FF0000'>%s punti nazionali</font></b></li>"),
	SESSION_INIZIATIVE_HOMEWORK_EURO("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha registrato una sessione casa/lavoro in %s di %s km guadagnando <b><font color='#6BA32D'>%s €</font></b> e <b><font color='#FF0000'>%s punti iniziativa</font></b> e <b><font color='#FF0000'>%s punti nazionali</font></b></li>"),
	RANKING_EURO("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha vinto il premio di <b><font color='#6BA32D'>%s €</font></b> nella classifica %s</li>"),
	RANKING_POINTS("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha vinto il premio di <b><font color='#FF0000'>%s punti</font></b> nella classifica %s</li></div>"),
	ACHIEVEMENT_EURO("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha vinto la coppa %s pari a <b><font color='#6BA32D'>%s €</font></b></li>"),
	ACHIEVEMENT_POINTS("<li><b><u>%s</u></b><br/>%s dell'iniziativa %s ha vinto la coppa %s pari a <b><font color='#FF0000'>%s punti</font></b></li>");

	private final String text;

	LogWallTemplate(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
