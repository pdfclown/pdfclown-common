/*
  SPDX-FileCopyrightText: 2025-2026 Stefano Chizzolini and contributors

  SPDX-License-Identifier: LGPL-3.0-only

  This file (TextsTest.java) is part of pdfclown-common-util module in pdfClown Common project
  <https://github.com/pdfclown/pdfclown-common>

  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER. If you reuse (entirely or partially)
  this file, you MUST add your own copyright notice in a separate comment block above this file
  header, listing the main changes you applied to the original source.
 */
package org.pdfclown.common.util.io;

import static java.util.Arrays.asList;
import static org.pdfclown.common.util.Exceptions.runtime;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.pdfclown.common.util.__test.BaseTest;

/**
 * @author Stefano Chizzolini
 */
class TextsTest extends BaseTest {
  @Test
  void textCoords() {
    var text =
        """
            ЗАГАЛЬНА ДЕКЛАРАЦІЯ ПРАВ ЛЮДИНІ
            ПРЕАМБУЛА
            Беручи до уваги, що визнання гідності, яка властива всім членам людскої сім'ї, і рівних та невід'ємних їх прав є основою свободі, справедливості та загального миру; і
            беручи до уваги, що зневажання і нехтування правами людини призвели до варварських актів, які обурюють совість людства, і що створення такого світу, в якому люди будуть мати свободу слова і переконань і будуть вільні від страху і нужди, проголошено як високе прагнення людей; і
            беручи до уваги, що необхідно, щоб права людини охоронялися силою закону з метою забезпечення того, щоб людина не була змушена вдаватиця як до останнього засобу до повстання проти тиранії і гноблення; і
            беручи до уваги, що необхідно сприяти розвиткові дружніх відносин між народами; і
            беручи до уваги, що народи Об'єднаних Націй підтвердили в Статуті свою віру в основні права людини, в гідність і цінність людської осови і в рівноправність чоловіків і жінок та вирішили сприяти соціальному прогресові і поліпшенню умов життя при більшій свободі; і
            беручи до уваги. що держави-члени зобов'язались сприяти у співробітництві з Організацією Об'єднаних Націй загальній повазі і додержанню прав людини і основних свсбод; і
            беручи до уваги, що загальне розуміння характеру цих прав і свобод має величезне значення для повного биконання цього зобов'язання,
            ГЕНЕРАЛЬНА АСАМБЛЕЯ
            проголошує цю Загальну Декларацію Ппав Людини як завдання, до виконання якого повинні прагнути всі народи і всі держави з тим, щоб кожна людина і кожний орган суспільства, завжди маючи на увазі цю Декларацію, прагнули шляхом освіти сприяти поважанню цих прав і свобод і забезпеченню, шляхом національних і міжнародних прогресивних заходів, загального і ефективного візнання і здійснення їх як серед народів держав - членів Організації, так і серед народів територій, що перебувають під їх юрисдикцією.""";

    combinationVerifier.verify(
        (subtext) -> {
          try {
            return Texts.textCoords(new StringReader(text), text.indexOf(subtext)).orElseThrow();
          } catch (IOException ex) {
            throw runtime(ex);
          }
        },
        List.of("subtext"),
        // subtext
        asList(
            "Something not existing in the text",
            "ЗАГАЛЬНА ДЕКЛАРАЦІЯ ПРАВ ЛЮДИНІ",
            "народи Об'єднаних Націй підтвердили в Статуті свою віру в основні права людини",
            "проголошує цю Загальну Декларацію Ппав Людини"));
  }
}