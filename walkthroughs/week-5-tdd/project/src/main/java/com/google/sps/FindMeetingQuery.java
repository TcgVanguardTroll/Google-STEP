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

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    /* Array of queries representing times to meet. */
    List<TimeRange> query = new ArrayList<>();

    // Copy of events used for iterating and sorting.
    List<Event> eventsCopy = new ArrayList<>(events);

    int numberOfEvents = events.size();

    int currentTime = START_OF_DAY;

    int nextTime = START_OF_DAY;

    int desiredDuration = (int) request.getDuration();

    // Collection of string representing who is to attend the requested meeting.
    Collection<String> requestAttendees = request.getAttendees();

    // Check if duration of event is longer than a whole day and if so return empty.
    if ((int) request.getDuration() > WHOLE_DAY.duration()) {
      return query;
    }

    // Check if there are any events happening at all if so return whole day query.
    if (numberOfEvents == 0) {
      // Adds a 24 hour time range to query.
      query.add(TimeRange.WHOLE_DAY);
      return query;
    }
    //  Event counter used whilst iterating.
    int eventCounter = 0;

    // Sorts the even in chronological order.
    eventsCopy.sort((e1, e2) -> ORDER_BY_START.compare(e1.getWhen(), e2.getWhen()));

    // Iterating through input event collection.
    for (Event event : eventsCopy) {
      currentTime = nextTime;

      // Collection of the people who are required to attend meeting.
      Set<String> eventAttendees = event.getAttendees();

      // Increment event idx by one
      eventCounter += 1;

      // If the the only attendee is someone different than the person looking to book a meeting.
      if (Collections.disjoint(eventAttendees, requestAttendees)) {
        query.add(TimeRange.WHOLE_DAY);
        continue;
      }

      // Fetch event start time.
      int eventStart = event.getWhen().start();
      // Fetch event end time.
      int eventEnd = event.getWhen().end();

      // If there is time before the meeting starts.
      if (currentTime <= eventStart) {
        //   and there is enough time to validate this meeting
        if (eventStart - currentTime >= desiredDuration) {
          // add it to the query and update time.
          query.add(TimeRange.fromStartEnd(currentTime, eventStart, /* inclusive= */ false));
        }
        // Storing value of next time
        nextTime = eventEnd;
      }

      // Check for overlapping/nested events
      if ((currentTime > eventStart) && (currentTime < eventEnd)) nextTime = eventEnd;

      // If at the last event and there is still time in the day.
      if (eventCounter == numberOfEvents) {
        if (nextTime <= END_OF_DAY) {
          query.add(TimeRange.fromStartEnd(nextTime, END_OF_DAY, /* inclusive= */ true));
        }
      }
    }

    //  Return the meetings.
    return query;
  }
}
