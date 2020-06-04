DROP TABLE IF EXISTS Plays;
DROP TABLE IF EXISTS ShowSeats;
DROP TABLE IF EXISTS Payments;
DROP TABLE IF EXISTS Bookings;
DROP TABLE IF EXISTS Shows;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Movies;
DROP TABLE IF EXISTS CinemaSeats;
DROP TABLE IF EXISTS Theaters;
DROP TABLE IF EXISTS Cinemas;
DROP TABLE IF EXISTS Cities;

-- Entities

CREATE TABLE Cities (
    city_id BIGINT NOT NULL,
    city_name VARCHAR(64) NOT NULL,
    city_state CHAR(2) NOT NULL,
    zip_code NUMERIC(5) NOT NULL,
    PRIMARY KEY(city_id)
);

CREATE TABLE Cinemas (
    cid BIGINT NOT NULL,  -- Cinema ID
    city_id BIGINT NOT NULL,
    cname VARCHAR(64) NOT NULL,  -- Cinema name
    tnum INTEGER NOT NULL,  -- Number of theaters
    PRIMARY KEY(cid),
    FOREIGN KEY(city_id) REFERENCES Cities(city_id)
);

CREATE TABLE Theaters (
    tid BIGINT NOT NULL,  -- Theater ID
    cid BIGINT NOT NULL,  -- Cinema ID
    tname VARCHAR(64) NOT NULL,  -- Theater name
    tseats BIGINT NOT NULL,  -- Number of seats in the theater
    PRIMARY KEY(tid),
    FOREIGN KEY(cid) REFERENCES Cinemas(cid)
);

CREATE TABLE CinemaSeats (
    csid BIGINT NOT NULL,  -- Cinema seat ID
    tid BIGINT NOT NULL,  -- Theater ID
    sno INTEGER NOT NULL,  -- Seat number in the theater
    stype VARCHAR(16) NOT NULL,  -- Seat type
    PRIMARY KEY(csid),
    FOREIGN KEY(tid) REFERENCES Theaters(tid)
);

CREATE TABLE Movies (
    mvid BIGINT NOT NULL,  -- Movie ID
    title VARCHAR(128) NOT NULL,  -- Movie title
    rdate DATE NOT NULL,  -- Release date
    country VARCHAR(64) NOT NULL,  -- Release country
    description TEXT,
    duration INTEGER,  -- In seconds
    lang CHAR(2),  -- Language code, such as en, de
    genre VARCHAR(16),
    PRIMARY KEY(mvid)
);

CREATE TABLE Users (
    email VARCHAR(64) NOT NULL,
    lname VARCHAR(32) NOT NULL,  -- Last name
    fname VARCHAR(32) NOT NULL,  -- First name
    phone NUMERIC(10, 0),
    pwd CHAR(64) NOT NULL,  -- SHA256 hash of password
    PRIMARY KEY(email)
);

CREATE TABLE Shows (
    sid BIGINT NOT NULL,  -- Show ID
    mvid BIGINT NOT NULL,  -- Movie ID
    sdate DATE NOT NULL,  -- Show date
    sttime TIME NOT NULL,  -- Start time
    edtime TIME NOT NULL,  -- End time
    PRIMARY KEY(sid),
    FOREIGN KEY(mvid) REFERENCES Movies(mvid)
);

CREATE TABLE Bookings (
    bid BIGINT NOT NULL,  -- Booking ID
    status VARCHAR(16) NOT NULL,
    bdatetime TIMESTAMPTZ NOT NULL,  -- Booking date and time
    seats INTEGER NOT NULL,  -- Number of seats booked
    sid BIGINT NOT NULL,  -- Show ID
    email VARCHAR(64) NOT NULL,  -- User account
    PRIMARY KEY(bid),
    FOREIGN KEY(sid) REFERENCES Shows(sid) ON DELETE CASCADE,
    FOREIGN KEY(email) REFERENCES Users(email)    
    -- A booking has at most one payment is enforced in Payments via UNIQUE
);

CREATE TABLE Payments (
    pid BIGINT NOT NULL,  -- Payment ID
    bid BIGINT NOT NULL,  -- Booking ID
    pmethod VARCHAR(32) NOT NULL,
    pdatetime TIMESTAMPTZ NOT NULL,  -- Payment date and time
    amount REAL NOT NULL,
    trid BIGINT,  -- Transaction ID
    PRIMARY KEY(pid),
    FOREIGN KEY(bid) REFERENCES Bookings(bid) ON DELETE CASCADE,
    UNIQUE(bid)  -- No two payments can have the same booking
);

CREATE TABLE ShowSeats (
    ssid BIGINT NOT NULL,  -- Show seat ID
    sid BIGINT NOT NULL,  -- Show ID
    csid BIGINT NOT NULL, -- Cinema seat ID
    bid BIGINT, -- Booking ID
    price REAL NOT NULL,
    PRIMARY KEY(ssid),
    FOREIGN KEY(sid) REFERENCES Shows(sid),
    FOREIGN KEY(csid) REFERENCES CinemaSeats(csid),
    FOREIGN KEY(bid) REFERENCES Bookings(bid),
    UNIQUE(sid, csid)  -- The same seat can only be booked once for the same show
);


-- Relations

CREATE TABLE Plays (
    sid BIGINT NOT NULL,  -- Show ID
    tid BIGINT NOT NULL,  -- Theater ID
    PRIMARY KEY(sid, tid),
    FOREIGN KEY(sid) REFERENCES Shows(sid),
    FOREIGN KEY(tid) REFERENCES Theaters(tid)
);


----------------------------
-- INSERT DATA STATEMENTS --
----------------------------

COPY Cities (
	city_id,
	city_name,
	city_state,
	zip_code
)
FROM 'Cities.csv'
WITH DELIMITER ',';

COPY Cinemas (
	cid,
	city_id,
	cname,
	tnum
)
FROM 'Cinemas.csv'
WITH DELIMITER ',';

COPY Theaters (
	tid,
	cid,
	tname,
	tseats
)
FROM 'Theaters.csv'
WITH DELIMITER ',';

COPY CinemaSeats (
	csid,
	tid,
	sno,
	stype
)
FROM 'CinemaSeats.csv'
WITH DELIMITER ',';

COPY Movies  (
	mvid,
	title,
	rdate,
	country,
	description,
	duration,
	lang,
	genre
)
FROM 'Movies.csv'
WITH DELIMITER ',';

COPY Users (
	email,
	lname,
	fname,
	phone,
	pwd 
)
FROM 'Users.csv'
WITH DELIMITER ',';

COPY Shows (
	sid,
	mvid,
	sdate,
	sttime,
	edtime
)
FROM 'Shows.csv'
WITH DELIMITER ',';

COPY Bookings (
    bid,
    status,
    bdatetime,
    seats,
    sid,
    email
)
FROM 'Bookings.csv'
WITH DELIMITER ',';

COPY Payments (
    pid,
    bid,
    pmethod,
    pdatetime,
    amount,
    trid
)
FROM 'Payments.csv'
WITH DELIMITER ',';

COPY ShowSeats (
    ssid,
    sid,
    csid,
    bid,
    price
)
FROM 'ShowSeats.csv'
WITH DELIMITER ',' NULL AS '';

COPY Plays (
    sid,
    tid
)
FROM 'Plays.csv'
WITH DELIMITER ',';
