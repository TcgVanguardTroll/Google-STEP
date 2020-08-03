// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";

  // All dates are the first day of the year 2020.
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);

  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void optionsForNoAttendees() {
    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Collections.singletonList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noOptionsForTooLongOfARequest() {
    // The duration should be longer than a day. This means there should be no options.
    int duration = TimeRange.WHOLE_DAY.duration() + 1;
    MeetingRequest request = new MeetingRequest(Collections.singletonList(PERSON_A), duration);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Collections.emptyList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void eventSplitsRestriction() {
    // The event should split the day into two options (before and after the event).
    Collection<Event> events =
       Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_A)));

    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsidered() {
    // Have each person have different events. We should see two options because each person has
    // split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEvents() {
    // Have an event for each person, but have their events overlap. We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
                Collections.singletonList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void nestedEvents() {
    // Have an event for each person, but have one person's event fully contain another's event. We
    // should see two options.
    //
    // Events  :       |----A----|
    //                   |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void doubleBookedPeople() {
    // Have one person, but have them registered to attend two events at the same time.
    //
    // Events  :       |----A----|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_A)));

    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoom() {
    // Have one person, but make it so that there is just enough room at one point in the day to
    // have the meeting.
    //
    // Events  : |--A--|     |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Collections.singletonList(PERSON_A)));

    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_A), DURATION_30_MINUTES);

    Collection<com.google.sps.TimeRange> actual = query.query(events, request);
    Collection<com.google.sps.TimeRange> expected =
        Arrays.asList(com.google.sps.TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void ignoresPeopleNotAttending() {
    // Add an event, but make the only attendee someone different from the person looking to book
    // a meeting. This event should not affect the booking.
    Collection<com.google.sps.Event> events =
        Arrays.asList(
            new com.google.sps.Event(
                "Event 1",
                com.google.sps.TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_A)));
    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Collections.singletonList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noConflicts() {
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Collections.singletonList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoom() {
    // Have one person, but make it so that there is not enough room at any point in the day to
    // have the meeting.
    //
    // Events  : |--A-----| |-----A----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Collections.singletonList(PERSON_A)));

    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_A), DURATION_60_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Collections.emptyList();

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAttendeeConsidered() {
    // Add an optional attendee C who has an all-day event. The same three time slots should be
    // returned as when C was not invited.

    String testPerson = "Person Jordan";

    Collection<com.google.sps.Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Collections.singletonList(PERSON_B)),
            new Event("Event 3", TimeRange.WHOLE_DAY, Collections.singletonList(testPerson)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    request.addOptionalAttendee(testPerson);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            com.google.sps.TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            com.google.sps.TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            com.google.sps.TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }


  @Test
  public void optionalJustEnoughRoom() {

    String testPerson = "Person Nadroj";

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartDuration(TIME_0830AM, 15),
                Collections.singletonList(testPerson)));

    MeetingRequest request =
        new MeetingRequest(Collections.singletonList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(testPerson);

    Collection<com.google.sps.TimeRange> actual = query.query(events, request);
    Collection<com.google.sps.TimeRange> expected =
        Arrays.asList(com.google.sps.TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noMandatoryAttendeesNoGaps() {
    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, false),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true),
                Collections.singletonList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Collections.emptyList(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<com.google.sps.TimeRange> actual = query.query(events, request);

    Collection<com.google.sps.TimeRange> expected = Collections.emptyList();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noMandatoryAttendeesGaps() {
    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0930AM, false),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_1000AM, 30),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartDuration(TimeRange.getTimeInMinutes(16, 0), 30),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(
                    TimeRange.getTimeInMinutes(17, 0), TimeRange.END_OF_DAY, true),
                Collections.singletonList(PERSON_A)),
            new Event(
                "Event 1.5",
                TimeRange.fromStartDuration(TimeRange.getTimeInMinutes(9, 0), 30),
                Collections.singletonList(PERSON_B)),
            new Event(
                "Event 5",
                TimeRange.fromStartDuration(TimeRange.getTimeInMinutes(14, 0), 120),
                Collections.singletonList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Collections.emptyList(), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<com.google.sps.TimeRange> actual = query.query(events, request);

    Collection<com.google.sps.TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(
                TimeRange.getTimeInMinutes(9, 30), TimeRange.getTimeInMinutes(10, 0), false),
            TimeRange.fromStartEnd(
                TimeRange.getTimeInMinutes(10, 30), TimeRange.getTimeInMinutes(14, 0), false),
            TimeRange.fromStartEnd(
                TimeRange.getTimeInMinutes(16, 30), TimeRange.getTimeInMinutes(17, 0), false));
    Assert.assertEquals(expected, actual);
  }
}
