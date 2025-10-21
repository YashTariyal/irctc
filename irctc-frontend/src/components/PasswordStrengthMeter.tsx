import React, { useState, useEffect } from 'react';
import {
  Box,
  LinearProgress,
  Typography,
  Chip,
  Alert,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import {
  CheckCircle,
  Cancel,
  Warning,
  Security,
} from '@mui/icons-material';

interface PasswordStrengthMeterProps {
  password: string;
  onValidationChange?: (isValid: boolean, errors: string[], warnings: string[]) => void;
}

interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

const PasswordStrengthMeter: React.FC<PasswordStrengthMeterProps> = ({
  password,
  onValidationChange,
}) => {
  const [validation, setValidation] = useState<ValidationResult>({
    valid: false,
    errors: [],
    warnings: [],
  });
  const [strength, setStrength] = useState(0);

  useEffect(() => {
    validatePassword(password);
  }, [password]);

  const validatePassword = async (pwd: string) => {
    if (!pwd) {
      setValidation({ valid: false, errors: [], warnings: [] });
      setStrength(0);
      onValidationChange?.(false, [], []);
      return;
    }

    // Client-side validation
    const errors: string[] = [];
    const warnings: string[] = [];

    // Length check
    if (pwd.length < 8) {
      errors.push('Password must be at least 8 characters long');
    } else if (pwd.length > 128) {
      errors.push('Password must not exceed 128 characters');
    }

    // Character requirements
    if (!/[A-Z]/.test(pwd)) {
      errors.push('Password must contain at least one uppercase letter');
    }
    if (!/[a-z]/.test(pwd)) {
      errors.push('Password must contain at least one lowercase letter');
    }
    if (!/[0-9]/.test(pwd)) {
      errors.push('Password must contain at least one digit');
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pwd)) {
      errors.push('Password must contain at least one special character');
    }

    // Common password check
    const commonPasswords = [
      'password', '123456', '123456789', 'qwerty', 'abc123', 'password123',
      'admin', 'letmein', 'welcome', 'monkey', '1234567890', 'password1',
    ];
    if (commonPasswords.some(common => pwd.toLowerCase().includes(common))) {
      errors.push('Password is too common and easily guessable');
    }

    // Repeating characters
    if (/(.)\1{2,}/.test(pwd)) {
      warnings.push('Password contains repeating characters which may reduce security');
    }

    // Sequential patterns
    if (/(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)/i.test(pwd)) {
      warnings.push('Password contains sequential patterns which may reduce security');
    }

    const isValid = errors.length === 0;
    const strengthScore = calculateStrength(pwd);

    const result = { valid: isValid, errors, warnings };
    setValidation(result);
    setStrength(strengthScore);
    onValidationChange?.(isValid, errors, warnings);
  };

  const calculateStrength = (pwd: string): number => {
    let score = 0;
    
    // Length scoring
    if (pwd.length >= 8) score += 20;
    if (pwd.length >= 12) score += 10;
    if (pwd.length >= 16) score += 10;
    
    // Character variety scoring
    if (/[a-z]/.test(pwd)) score += 10;
    if (/[A-Z]/.test(pwd)) score += 10;
    if (/[0-9]/.test(pwd)) score += 10;
    if (/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pwd)) score += 10;
    
    // Bonus for mixed case and numbers
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score += 10;
    if (/[0-9]/.test(pwd) && /[a-zA-Z]/.test(pwd)) score += 10;
    
    // Penalty for common patterns
    if (/(.)\1{2,}/.test(pwd)) score -= 20;
    if (/(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)/i.test(pwd)) score -= 15;
    
    return Math.max(0, Math.min(100, score));
  };

  const getStrengthColor = (score: number) => {
    if (score < 30) return 'error';
    if (score < 60) return 'warning';
    if (score < 80) return 'info';
    return 'success';
  };

  const getStrengthLabel = (score: number) => {
    if (score < 30) return 'Very Weak';
    if (score < 60) return 'Weak';
    if (score < 80) return 'Good';
    return 'Strong';
  };

  return (
    <Box sx={{ mt: 2 }}>
      {/* Password Strength Meter */}
      <Box sx={{ mb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
          <Security sx={{ mr: 1, fontSize: 20 }} />
          <Typography variant="body2" color="text.secondary">
            Password Strength
          </Typography>
          <Chip
            label={getStrengthLabel(strength)}
            color={getStrengthColor(strength) as any}
            size="small"
            sx={{ ml: 'auto' }}
          />
        </Box>
        <LinearProgress
          variant="determinate"
          value={strength}
          color={getStrengthColor(strength) as any}
          sx={{ height: 8, borderRadius: 4 }}
        />
      </Box>

      {/* Validation Results */}
      {password && (
        <Box>
          {/* Errors */}
          {validation.errors.length > 0 && (
            <Alert severity="error" sx={{ mb: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Password Requirements Not Met:
              </Typography>
              <List dense>
                {validation.errors.map((error, index) => (
                  <ListItem key={index} sx={{ py: 0 }}>
                    <ListItemIcon sx={{ minWidth: 32 }}>
                      <Cancel color="error" fontSize="small" />
                    </ListItemIcon>
                    <ListItemText primary={error} />
                  </ListItem>
                ))}
              </List>
            </Alert>
          )}

          {/* Warnings */}
          {validation.warnings.length > 0 && (
            <Alert severity="warning" sx={{ mb: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Security Recommendations:
              </Typography>
              <List dense>
                {validation.warnings.map((warning, index) => (
                  <ListItem key={index} sx={{ py: 0 }}>
                    <ListItemIcon sx={{ minWidth: 32 }}>
                      <Warning color="warning" fontSize="small" />
                    </ListItemIcon>
                    <ListItemText primary={warning} />
                  </ListItem>
                ))}
              </List>
            </Alert>
          )}

          {/* Success */}
          {validation.valid && (
            <Alert severity="success">
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <CheckCircle sx={{ mr: 1 }} />
                <Typography variant="body2">
                  Password meets all security requirements!
                </Typography>
              </Box>
            </Alert>
          )}
        </Box>
      )}
    </Box>
  );
};

export default PasswordStrengthMeter;
