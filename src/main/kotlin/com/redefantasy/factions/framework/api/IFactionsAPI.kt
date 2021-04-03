package com.redefantasy.factions.framework.api

import java.util.*

/**
 * @author Gutyerrez
 */
interface IFactionsAPI<F, P> {

    fun F.getMembersFromFaction(): List<P>

    fun getMPlayer(uuid: UUID?): P?

    fun P.getFaction(): F?

}
