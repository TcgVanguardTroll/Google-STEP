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

import java.util.*;

public final class FindMeetingQuery {

  private static final int START_OF_DAY = TimeRange.START_OF_DAY;
  private static final int END_OF_DAY = TimeRange.END_OF_DAY;
  private static final TimeRange WHOLE_DAY = TimeRange.WHOLE_DAY;
  private static final Comparator<TimeRange> ORDER_BY_START = TimeRange.ORDER_BY_START;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    // Array of queries representing times to meet.
    List<TimeRange> query = new ArrayList<>();

    int desiredDuration = (int) request.getDuration();

    // Collection of string representing who is to attend the requested meeting.
    Collection<String> allAttendees = new ArrayList<>(request.getAttendees());
    // Collection of strings representing who are optional to the meeting.
    Collection<String> optionalAttendees = new ArrayList<>(request.getOptionalAttendees());
    // Combine the optional and mandatory attendees to a single collection
    allAttendees.addAll(optionalAttendees);

    // Check if duration of event is longer than a whole day and if so return empty.
    if ((int) request.getDuration() > WHOLE_DAY.duration()) {
      return query;
    }

    // Check if there are any events happening at all if so return whole day query.
    if (events.size() == 0) {
      query.add(com.google.sps.TimeRange.WHOLE_DAY);
      return query;
    }

    // Copy of events used for iterating and sorting.
    List<Event> eventsCopy = new ArrayList<>(events);

    // Sorts the even in chronological order.
    eventsCopy.sort((e1, e2) -> ORDER_BY_START.compare(e1.getWhen(), e2.getWhen()));

    int nextTime = START_OF_DAY;

    // Iterating through input event collection.
    for (com.google.sps.Event event : eventsCopy) {
      int currentTime = nextTime;

      // Collection of the people who can attend meeting.
      Set<String> eventAttendees = event.getAttendees();

      // If the none of the attendees are capable of attending skip this event
      if (Collections.disjoint(eventAttendees, allAttendees)) {
        continue;
      }

      // Fetch event start time.
      int eventStart = event.getWhen().start();
      // Fetch event end time.
      int eventEnd = event.getWhen().end();

      // If this is a valid time range then add it to query .
      if (currentTime <= eventStart) {
        if (eventStart - currentTime >= desiredDuration) {
          query.add(TimeRange.fromStartEnd(currentTime, eventStart, /* inclusive= */ false));
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
      query.add(TimeRange.fromStartEnd(nextTime, END_OF_DAY, /* inclusive= */ true));
    }
    return query;
  }
}
