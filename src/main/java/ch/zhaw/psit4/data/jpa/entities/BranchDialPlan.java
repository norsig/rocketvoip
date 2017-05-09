package ch.zhaw.psit4.data.jpa.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Created by beni on 25.04.17.
 */

@Entity
@Table(name = "BRANCH_DIALPLAN")
@Setter
@Getter
public class BranchDialPlan {
    @Id
    @GeneratedValue
    @Column(name = "BRANCH_DIALPLAN_ID")
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "DIALPLAN_ID")
    private DialPlan dialPlan;

    @Column
    private int buttonNumber;

    protected BranchDialPlan() {
        //intentionally empty
    }

    public BranchDialPlan(DialPlan dialPlan, int buttonNumber) {
        this.dialPlan = dialPlan;
        this.buttonNumber = buttonNumber;
    }
}
