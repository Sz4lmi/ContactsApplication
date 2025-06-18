import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

/**
 * Service for centralized logging in the application.
 * Provides methods for logging messages at different levels.
 * Respects environment configuration to control debug logging in production.
 */
@Injectable({
  providedIn: 'root'
})
export class LoggingService {

  constructor() { }

  /**
   * Log debug information.
   * These logs will only appear in non-production environments.
   *
   * @param message The message to log
   * @param optionalParams Additional parameters to log
   */
  debug(message: any, ...optionalParams: any[]): void {
    if (environment.enableDebugLogging) {
      console.log(message, ...optionalParams);
    }
  }

  /**
   * Log informational messages.
   *
   * @param message The message to log
   * @param optionalParams Additional parameters to log
   */
  info(message: any, ...optionalParams: any[]): void {
    console.info(message, ...optionalParams);
  }

  /**
   * Log warning messages.
   *
   * @param message The message to log
   * @param optionalParams Additional parameters to log
   */
  warn(message: any, ...optionalParams: any[]): void {
    console.warn(message, ...optionalParams);
  }

  /**
   * Log error messages.
   * These will appear in all environments including production.
   *
   * @param message The message to log
   * @param optionalParams Additional parameters to log
   */
  error(message: any, ...optionalParams: any[]): void {
    console.error(message, ...optionalParams);
  }
}
