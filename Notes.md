# Data
    Note that we have a tuple for each calendar week (=0 if there is no match played in that week)
    Week_idx: weeks since monday 1/3/2001 
    Matches: actually played (0 means rest week)
    Minutes: total match minutes
    so_won: service points won
    sp_played: service points played
    rp_played: return points played
    rp_won: return points won
    inj: is a nice field which tells us if the player lost a match by retirement or default that week
    wo: 1 if the player had to withdraw before playing
    min_1w is just a copy of minutes played throughout the week (redundant)
    min _4w: minutes played over the past 4 weeks
    min_12w: minutes played over the past 12 weeks
    last_played: most recent week with a match
    weeks_since: weeks_idx - last_played, if 0 means the player played this week
    pct_f: all points won across serve and return over the past 8 weeks, NaN if < 150 points were played in this window
    pct_b: same measure over 52 weeks (same 150 point min)
    form_delta: pct_f - pct_b, positive means player is winning more points than their yearly norm (careful of just playing more tennis) 

# Creating the injury probabilities
    1) Seperate into 3 different age groups (based on dataset range)
    2) Proxy for a long-term injury is more than 3 weeks from playing a tournament (does not include extended New Year holiday stretch)
    3) 2020 excluded (COVID)
    4) Used a wilson interval to show confidence (take into account the observation limits)
