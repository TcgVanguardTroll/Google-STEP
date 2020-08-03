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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class FindMeetingQuery {

  private static final int START_OF_DAY = TimeRange.START_OF_DAY;
  private static final int END_OF_DAY = TimeRange.END_OF_DAY;
  private static final TimeRange WHOLE_DAY = TimeRange.WHOLE_DAY;
  private static final Comparator<TimeRange> ORDER_BY_START = TimeRange.ORDER_BY_START;

  public Collection<TimeRange> query(
      Collection<Event> events, MeetingRequest request) {

    // Array of queries representing times to meet.
    List<TimeRange> optionalTimeRanges;

    // Array of queries representing times to meet.
    List<TimeRange> mandatoryTimeRanges;

    // Array of queries representing times to meet.
    List<TimeRange> allTimeRanges;

    int desiredDuration = (int) request.getDuration();

    // Collection of string representing who is to attend the requested meeting.
    Collection<String> mandatoryAttendees = new ArrayList<>(request.getAttendees());

    // Collection of strings representing who are optional to the meeting.
    Collection<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());

    // Collection of Strings representing all attendees ( Mandatory and Optional )
    Collection<String> allAttendees =
        combineMandatoryAndOptional(mandatoryAttendees, optionalAttendees);

    // Check if duration of event is longer than a whole day and if so return empty.
    if ((int) request.getDuration() > WHOLE_DAY.duration()) {
      return Collections.emptyList();
    }

    // If there are no mandatory attendees , return lists of the optional attendees time ranges.
    if (mandatoryAttendees.isEmpty()) {
        return getTimeRanges(events, optionalAttendees, desiredDuration);
    }

    // If there are no optional attendees , return lists of the mandatory attendees time ranges.
    if (optionalAttendees.isEmpty()) {
        return getTimeRanges(events, mandatoryAttendees, desiredDuration);
    }

    allTimeRanges =
        (ArrayList<TimeRange>) getTimeRanges(events, allAttendees, desiredDuration);
    if (allTimeRanges.isEmpty()) {
        return getTimeRanges(events, mandatoryAttendees, desiredDuration);
    }
    return allTimeRanges;
  }

 public List<TimeRange> getTimeRanges(
      Collection<Event> events, Collection<String> attendees, int desiredDuration) {

    // Array of queries representing times to meet.
    List<TimeRange> timeRanges = new ArrayList<>();

    int nextTime = START_OF_DAY;

    // Copy of events used for iterating and sorting.
    List<Event> eventsCopy = new ArrayList<>(events);

    // Sorts the even in chronological order.
    eventsCopy.sort((e1, e2) -> ORDER_BY_START.compare(e1.getWhen(), e2.getWhen()));

    // Check if there are any events happening at all if so return whole day query.
    if (events.isEmpty()) {
      timeRanges.add(TimeRange.WHOLE_DAY);
      return timeRanges;
    }

    // Iterating through input event collection.
    for (Event event : eventsCopy) {
      int currentTime = nextTime;

      // Collection of the people who can attend meeting.
      Set<String> eventAttendees = event.getAttendees();

      // If the none of the attendees are capable of attending skip this event
      if (Collections.disjoint(eventAttendees, attendees)) {
        continue;
      }

      // Fetch event start time.
      int eventStart = event.getWhen().start();
      // Fetch event end time.
      int eventEnd = event.getWhen().end();

      // If this is a valid time range then add it to query .
      if (currentTime <= eventStart) {
        if (eventStart - currentTime >= desiredDuration) {
          timeRanges.add(
              TimeRange.fromStartEnd(
                  currentTime, eventStart, /* inclusive= */ false));
        }
        nextTime = eventEnd;
      }

      // Check for overlapping/nested events
      if (currentTime > eventStart && currentTime < eventEnd) {
        nextTime = eventEnd;
      }
    }
    // If at the last event and there is still time in the day.
    if (nextTime < END_OF_DAY) {
      timeRanges.add(TimeRange.fromStartEnd(nextTime, END_OF_DAY, /* inclusive= */ true));
    }
    return timeRanges;
  }

  public Collection<String> combineMandatoryAndOptional(
      Collection<String> mandatoryAttendees, Collection<String> optionalAttendees) {
    // Collection of Strings representing all attendees ( Mandatory and Optional )
    Collection<String> allAttendees = new ArrayList<>();
    allAttendees.addAll(optionalAttendees);
    allAttendees.addAll(mandatoryAttendees);
    return allAttendees;
  }
  public List<TimeRange> populateTimeRanges(
      Collection<com.google.sps.Event> events, Collection<String> attendees, int desiredDuration) {
    return getTimeRanges(events, attendees, desiredDuration);
  }
}
