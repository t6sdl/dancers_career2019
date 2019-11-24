package tokyo.t6sdl.dancerscareer2019.model.form;

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class InterviewForm {
	private int interviewId;
	@NotEmpty
	private String question;
	@NotEmpty
	private String answer;
	
	@Override
	public String toString() {
		if (Objects.equals(this.getQuestion(), null)) {
			this.setQuestion("");
		}
		if (Objects.equals(this.getAnswer(), null)) {
			this.setAnswer("");
		}
		return this.getQuestion() + this.getAnswer();
	}
}
