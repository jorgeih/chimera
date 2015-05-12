package de.uni_potsdam.hpi.bpt.bp2014.jcore;


/**
 * ********************************************************************************
 * <p/>
 * _________ _______  _        _______ _________ _        _______
 * \__    _/(  ____ \( (    /|(  ____ \\__   __/( (    /|(  ____ \
 * )  (  | (    \/|  \  ( || (    \/   ) (   |  \  ( || (    \/
 * |  |  | (__    |   \ | || |         | |   |   \ | || (__
 * |  |  |  __)   | (\ \) || | ____    | |   | (\ \) ||  __)
 * |  |  | (      | | \   || | \_  )   | |   | | \   || (
 * |\_)  )  | (____/\| )  \  || (___) |___) (___| )  \  || (____/\
 * (____/   (_______/|/    )_)(_______)\_______/|/    )_)(_______/
 * <p/>
 * ******************************************************************
 * <p/>
 * Copyright © All Rights Reserved 2014 - 2015
 * <p/>
 * Please be aware of the License. You may found it in the root directory.
 * <p/>
 * **********************************************************************************
 */

/**
 * Represents the abstract control node.
 */
public abstract class ControlNodeInstance {
    protected OutgoingBehavior outgoingBehavior;
    protected IncomingBehavior incomingBehavior;
    protected StateMachine stateMachine;
    protected int fragmentInstance_id;
    protected int controlNodeInstance_id;
    protected int controlNode_id;

    /**
     * Skips the control node.
     *
     * @return true if the skip success. false if not.
     */
    public abstract boolean skip();

    /**
     * Terminates the control node.
     *
     * @return true if the skip success. false if not.
     */
    public abstract boolean terminate();
    // *************************************** Getter ***************************************//

    /**
     *
     * @return
     */
    public OutgoingBehavior getOutgoingBehavior() {
        return outgoingBehavior;
    }

    /**
     *
     * @return
     */
    public IncomingBehavior getIncomingBehavior() {
        return incomingBehavior;
    }

    /**
     *
     * @return
     */
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     *
     * @return
     */
    public int getFragmentInstance_id() {
        return fragmentInstance_id;
    }

    /**
     *
     * @return
     */
    public int getControlNodeInstance_id() {
        return controlNodeInstance_id;
    }

    /**
     *
     * @return
     */
    public int getControlNode_id() {
        return controlNode_id;
    }
}
