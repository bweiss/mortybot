/*
 * MortyBot - An IRC bot built on the PircBotX framework.
 * Copyright © 2022 Brian Weiss (brian@hatemachine.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hatemachine.mortybot.util;

import net.hatemachine.mortybot.config.BotDefaults;
import net.hatemachine.mortybot.config.BotProperties;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/**
 * Factory class to generate password encoders.
 */
public class PasswordEncoderFactory {

    public enum EncoderType {
        ARGON2,
        BCRYPT,
        PKDF2
    }

    private PasswordEncoderFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets a password encoder for creating and checking password hashes.
     *
     * @return a PasswordEncoder object of the default type
     */
    public static PasswordEncoder getEncoder() {
        BotProperties props = BotProperties.getBotProperties();
        String encoderType = props.getStringProperty("password.encoder", BotDefaults.PASSWORD_ENCODER);
        return getEncoder(Enum.valueOf(PasswordEncoderFactory.EncoderType.class, encoderType));
    }

    /**
     * Gets a password encoder for creating and checking password hashes.
     *
     * @param type the type of encoder to use (possible types: ARGON2, BCRYPT, PKDF2)
     * @return a PasswordEncoder object of the specified type
     */
    public static PasswordEncoder getEncoder(EncoderType type) {
        PasswordEncoder encoder = null;

        if (type == EncoderType.ARGON2) {
            // For these parameters see the whitepaper (https://github.com/P-H-C/phc-winner-argon2/blob/master/argon2-specs.pdf), Section 9
            int saltLength = 128 / 8; // 128 bits
            int hashLength = 256 / 8; // 256 bits
            int parallelism = 1;
            int memoryInKb = 10 * 1024; // 10 MB
            int iterations = 10;

            encoder = new Argon2PasswordEncoder(saltLength, hashLength, parallelism, memoryInKb, iterations);

        } else if (type == EncoderType.BCRYPT) {
            encoder = new BCryptPasswordEncoder();

        } else if (type == EncoderType.PKDF2) {
            encoder = Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        }

        return encoder;
    }
}
