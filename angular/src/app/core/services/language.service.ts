import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';

export interface Language {
  code: string;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private readonly STORAGE_KEY = 'selected-language';
  
  public readonly availableLanguages: Language[] = [
    { code: 'en', name: 'English' },
    { code: 'gr', name: 'Ελληνικά' }
  ];

  private currentLanguageSubject = new BehaviorSubject<Language>(this.getDefaultLanguage());
  public currentLanguage$ = this.currentLanguageSubject.asObservable();

  constructor(private translateService: TranslateService) {
    this.initializeLanguage();
  }

  private initializeLanguage(): void {
    const savedLanguage = this.getSavedLanguage();
    const language = savedLanguage || this.getDefaultLanguage();
    
    this.translateService.setDefaultLang('en');
    this.setLanguage(language.code);
  }

  private getDefaultLanguage(): Language {
    return this.availableLanguages[0]; // English as default
  }

  private getSavedLanguage(): Language | null {
    const savedCode = localStorage.getItem(this.STORAGE_KEY);
    return savedCode ? this.availableLanguages.find(lang => lang.code === savedCode) || null : null;
  }

  public setLanguage(languageCode: string): void {
    const language = this.availableLanguages.find(lang => lang.code === languageCode);
    if (!language) return;

    this.translateService.use(languageCode);
    localStorage.setItem(this.STORAGE_KEY, languageCode);
    this.currentLanguageSubject.next(language);
  }

  public getCurrentLanguage(): Language {
    return this.currentLanguageSubject.value;
  }

  public switchLanguage(): void {
    const currentIndex = this.availableLanguages.findIndex(
      lang => lang.code === this.getCurrentLanguage().code
    );
    const nextIndex = (currentIndex + 1) % this.availableLanguages.length;
    this.setLanguage(this.availableLanguages[nextIndex].code);
  }
}
