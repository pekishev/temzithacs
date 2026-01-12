#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Пример реализации протокола сохранения настроек для теплового насоса
"""

import socket
import struct

# Типы пакетов
SEND_SETTINGS = 0x35  # 53
CHANGE_ONE_SETTING = 0x32  # 50
SEND_RTCSETTINGS = 0x37  # 55
CHANGE_ONE_RTCSETTING = 0x33  # 51
GET_SETTINGS = 0x34  # 52
GET_RTCSETTINGS = 0x36  # 54
GET_STATE = 0x30  # 48

# IP адрес по умолчанию
DEFAULT_IP = "192.168.4.1"
DEFAULT_PORT = 333  # Порт согласно официальной документации ТЭМЗИТ


def hex_to_bin(c):
    """Конвертация HEX символа в число"""
    if '0' <= c <= '9':
        return ord(c) - ord('0')
    if 'A' <= c <= 'F':
        return ord(c) - ord('A') + 10
    if 'a' <= c <= 'f':
        return ord(c) - ord('a') + 10
    return 0


def calc_crc(data, crc_index):
    """Вычисление CRC (контрольной суммы)"""
    crc = sum(data[i] & 0xFF for i in range(crc_index)) & 0xFF
    data[crc_index] = crc
    return data


def hex_string_to_bytes(hex_str):
    """Конвертация HEX строки в массив байтов"""
    result = []
    for i in range(0, len(hex_str), 2):
        byte_val = (hex_to_bin(hex_str[i]) << 4) | hex_to_bin(hex_str[i + 1])
        result.append(byte_val)
    return bytes(result)


def send_settings(sock, config_hex):
    """
    Отправка всех настроек (SEND_SETTINGS)
    
    Args:
        sock: TCP сокет
        config_hex: HEX строка с настройками (52 символа = 26 байт)
    
    Returns:
        bool: True если успешно
    """
    if len(config_hex) != 52:
        raise ValueError("Config должен быть 52 символа (26 байт)")
    
    packet = bytearray(32)
    packet[0] = SEND_SETTINGS
    packet[1] = 26  # Длина данных
    
    # Конвертация HEX строки в байты
    config_bytes = hex_string_to_bytes(config_hex)
    packet[2:28] = config_bytes
    
    # Заполнение резерва нулями
    packet[28:31] = b'\x00\x00\x00'
    
    # Вычисление CRC
    packet[31] = 0
    calc_crc(packet, 31)
    
    # Отправка
    sock.sendall(packet)
    return True


def change_one_setting(sock, param_num, param_value):
    """
    Изменение одного параметра (CHANGE_ONE_SETTING)
    
    Args:
        sock: TCP сокет
        param_num: Номер параметра (0-255)
        param_value: Значение параметра (0-255)
    
    Returns:
        bool: True если успешно
    """
    packet = bytearray(8)
    packet[0] = CHANGE_ONE_SETTING
    packet[1] = 0x00
    packet[2] = 0x00
    packet[3] = param_num & 0xFF
    packet[4] = 0x00
    packet[5] = param_value & 0xFF
    packet[6] = 0x00
    packet[7] = 0x00
    
    # Вычисление CRC
    calc_crc(packet, 7)
    
    # Отправка
    sock.sendall(packet)
    return True


def send_rtc_settings(sock, rtc_config_hex):
    """
    Отправка настроек расписания (SEND_RTCSETTINGS)
    
    Args:
        sock: TCP сокет
        rtc_config_hex: HEX строка с настройками расписания (80 символов = 40 байт)
    
    Returns:
        bool: True если успешно
    """
    if len(rtc_config_hex) != 80:
        raise ValueError("RTC config должен быть 80 символов (40 байт)")
    
    packet = bytearray(42)
    packet[0] = SEND_RTCSETTINGS
    packet[1] = 40  # Длина данных
    
    # Конвертация HEX строки в байты
    config_bytes = hex_string_to_bytes(rtc_config_hex)
    packet[2:42] = config_bytes
    
    # Вычисление CRC
    packet[41] = 0
    calc_crc(packet, 41)
    
    # Отправка
    sock.sendall(packet)
    return True


def receive_response(sock):
    """
    Получение ответа от устройства
    
    Args:
        sock: TCP сокет
    
    Returns:
        bytes: Ответ (64 байта) или None при ошибке
    """
    try:
        response = sock.recv(64)
        if len(response) != 64:
            return None
        
        # Проверка CRC
        crc_sum = sum(response[i] & 0xFF for i in range(62)) & 0xFFFF
        received_crc = (response[62] & 0xFF) | ((response[63] & 0xFF) << 8)
        
        if crc_sum != received_crc:
            print(f"Ошибка CRC: вычислено {crc_sum:04X}, получено {received_crc:04X}")
            return None
        
        return response
    except Exception as e:
        print(f"Ошибка при получении ответа: {e}")
        return None


def get_settings(sock):
    """Запрос текущих настроек"""
    packet = bytes([GET_SETTINGS])
    sock.sendall(packet)
    return receive_response(sock)


def get_state(sock):
    """Запрос текущего состояния"""
    packet = bytes([GET_STATE, 0x00])
    sock.sendall(packet)
    return receive_response(sock)


def get_word(data, offset):
    """Чтение 16-битного слова (little-endian)"""
    return (data[offset] & 0xFF) | ((data[offset + 1] & 0xFF) << 8)


def int16_to_float(value):
    """Преобразование int16 со знаком в float (деление на 10)"""
    if value >= 32768:
        value -= 65536
    return value / 10.0


def parse_sensor_data(data):
    """
    Парсинг данных сенсоров из ответа
    
    Args:
        data: Массив байтов (64 байта)
    
    Returns:
        dict: Словарь с данными сенсоров
    """
    if len(data) != 64:
        raise ValueError("Данные должны быть 64 байта")
    
    if data[0] != 0x00 and data[0] != 0x01:
        raise ValueError(f"Неверный тип ответа: {data[0]}")
    
    # Время (BCD формат)
    hour_lo = data[59] & 0x0F
    hour_hi = (data[59] >> 4) & 0x0F
    min_lo = data[60] & 0x0F
    min_hi = (data[60] >> 4) & 0x0F
    sec_lo = data[61] & 0x0F
    sec_hi = (data[61] >> 4) & 0x0F
    
    hour = hour_hi * 10 + hour_lo
    minute = min_hi * 10 + min_lo
    second = sec_hi * 10 + sec_lo
    
    # Основные данные
    state = get_word(data, 2)
    schedule = get_word(data, 4)
    Tout = int16_to_float(get_word(data, 6))
    Tin = get_word(data, 8) / 10.0
    Tf = get_word(data, 10) / 10.0
    Tb = get_word(data, 12) / 10.0
    Tcond = int(int16_to_float(get_word(data, 14)))
    Tevap = int(int16_to_float(get_word(data, 16)))
    Tgvs = get_word(data, 18) / 10
    
    # Расход
    flow_word = get_word(data, 20)
    Flow = flow_word & 0xFF
    Flow2 = (flow_word >> 8) & 0xFF
    
    # Частота компрессора
    comp_word = get_word(data, 24)
    CompFreq = comp_word & 0xFF
    CompFreq2 = (comp_word >> 8) & 0xFF
    
    # Мощность
    Pin = get_word(data, 30) / 10.0
    
    # Ошибки
    Failures = get_word(data, 32) & 0xFFFF
    
    # Второй контур
    Tf2 = get_word(data, 34) / 10.0
    Tb2 = get_word(data, 36) / 10.0
    Tcond2 = int(int16_to_float(get_word(data, 38)))
    Tevap2 = int(int16_to_float(get_word(data, 40)))
    
    # Флаги и настройки
    Dualmode = 1 if (data[44] & 1) == 0 else 0
    ControllerRevision = (data[45] & 0xFF) * 100 + (data[46] & 0xFF)
    ReceivedPage = data[47] & 0xFF
    ModeSet = data[48] & 0xFF
    Begin = data[49] & 0xFF
    End = data[50] & 0xFF
    TroomSet = data[51] & 0xFF
    TwaterSet = data[52] & 0xFF
    TgvsSet = data[53] & 0xFF
    GVSModeSet = data[56] & 0xFF
    
    # Вычисление выходной мощности
    Pout = ((Flow * 1.25 * 60.0 * (Tf - Tb)) / 100.0) / 10.0
    Pout2 = ((Flow2 * 1.25 * 60.0 * (Tf2 - Tb2)) / 100.0) / 10.0
    
    return {
        'time': f"{hour:02d}:{minute:02d}:{second:02d}",
        'state': state,
        'schedule': schedule,
        'Tout': Tout,
        'Tin': Tin,
        'Tf': Tf,
        'Tb': Tb,
        'Tcond': Tcond,
        'Tevap': Tevap,
        'Tgvs': Tgvs,
        'Flow': Flow,
        'Flow2': Flow2,
        'CompFreq': CompFreq,
        'CompFreq2': CompFreq2,
        'Pin': Pin,
        'Pout': Pout,
        'Pout2': Pout2,
        'Failures': Failures,
        'Tf2': Tf2,
        'Tb2': Tb2,
        'Tcond2': Tcond2,
        'Tevap2': Tevap2,
        'Dualmode': Dualmode,
        'ControllerRevision': ControllerRevision,
        'ReceivedPage': ReceivedPage,
        'ModeSet': ModeSet,
        'Begin': Begin,
        'End': End,
        'TroomSet': TroomSet,
        'TwaterSet': TwaterSet,
        'TgvsSet': TgvsSet,
        'GVSModeSet': GVSModeSet,
    }


def get_sensor_data(sock):
    """
    Получение и парсинг данных сенсоров
    
    Args:
        sock: TCP сокет
    
    Returns:
        dict: Словарь с данными сенсоров или None при ошибке
    """
    response = get_state(sock)
    if response is None:
        return None
    
    try:
        return parse_sensor_data(response)
    except Exception as e:
        print(f"Ошибка парсинга данных: {e}")
        return None


# Пример использования
if __name__ == "__main__":
    # Пример конфигурации
    config_example = "01101E00F6E7052300000A0500010613590F030000000F0000055A00"
    rtc_config_example = "00000010101400000000000000101014000000000000001010140000000000000010101400000000"
    
    try:
        # Создание соединения
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(5.0)  # Таймаут 5 секунд
        sock.connect((DEFAULT_IP, DEFAULT_PORT))
        
        print("Соединение установлено")
        
        # Пример 1: Отправка всех настроек
        print("\nОтправка всех настроек...")
        send_settings(sock, config_example)
        response = receive_response(sock)
        if response:
            print("Настройки успешно отправлены")
            print(f"Тип ответа: {response[0]}")
        else:
            print("Ошибка при получении ответа")
        
        # Пример 2: Изменение одного параметра
        print("\nИзменение параметра 0 (режим работы) на значение 1...")
        change_one_setting(sock, 0, 1)
        response = receive_response(sock)
        if response:
            print("Параметр успешно изменен")
        
        # Пример 3: Запрос текущих настроек
        print("\nЗапрос текущих настроек...")
        response = get_settings(sock)
        if response:
            print("Настройки получены")
            if response[0] == 2:  # SETTINGS ответ
                config_hex = ''.join(f'{b:02X}' for b in response[2:28])
                print(f"Config: {config_hex}")
        
        # Пример 4: Чтение данных сенсоров
        print("\nЧтение данных сенсоров...")
        sensor_data = get_sensor_data(sock)
        if sensor_data:
            print("Данные сенсоров получены:")
            print(f"  Время устройства: {sensor_data['time']}")
            print(f"  Состояние: {sensor_data['state']}")
            print(f"  Расписание: {sensor_data['schedule']}")
            print(f"  Температура наружного воздуха: {sensor_data['Tout']:.1f}°C")
            print(f"  Температура входящего воздуха: {sensor_data['Tin']:.1f}°C")
            print(f"  Температура подачи: {sensor_data['Tf']:.1f}°C")
            print(f"  Температура обратки: {sensor_data['Tb']:.1f}°C")
            print(f"  Температура конденсатора: {sensor_data['Tcond']}°C")
            print(f"  Температура испарителя: {sensor_data['Tevap']}°C")
            print(f"  Температура ГВС: {sensor_data['Tgvs']:.1f}°C")
            print(f"  Расход: {sensor_data['Flow']:.1f} л/мин")
            print(f"  Частота компрессора: {sensor_data['CompFreq']} Гц")
            print(f"  Мощность потребления: {sensor_data['Pin']:.1f} кВт")
            print(f"  Мощность на выходе: {sensor_data['Pout']:.1f} кВт")
            print(f"  Ошибки: {sensor_data['Failures']}")
            print(f"  Ревизия контроллера: {sensor_data['ControllerRevision']}")
            if sensor_data['Dualmode']:
                print(f"  Второй контур:")
                print(f"    Tf2: {sensor_data['Tf2']:.1f}°C")
                print(f"    Tb2: {sensor_data['Tb2']:.1f}°C")
                print(f"    Расход 2: {sensor_data['Flow2']:.1f} л/мин")
                print(f"    Мощность 2: {sensor_data['Pout2']:.1f} кВт")
        
        sock.close()
        print("\nСоединение закрыто")
        
    except socket.timeout:
        print("Таймаут соединения")
    except socket.error as e:
        print(f"Ошибка сети: {e}")
    except Exception as e:
        print(f"Ошибка: {e}")

