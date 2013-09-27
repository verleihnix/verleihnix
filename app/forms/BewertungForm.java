package forms;


import models.Ausleihe;
import play.data.validation.Constraints;

public class BewertungForm {
    @Constraints.Required
    public Ausleihe ausleihe;
    @Constraints.Required
    @Constraints.Min(1)
    @Constraints.Max(5)
    public int wertung;

}
