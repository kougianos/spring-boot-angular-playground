import { Component } from '@angular/core';
import { PublicApiService } from '../core/services/public-api.service';

interface BankHolidayEvent {
  title: string;
  date: string;
  notes: string;
  bunting: boolean;
}

interface DivisionData {
  division: string;
  events: BankHolidayEvent[];
}

@Component({
  selector: 'app-public-apis',
  templateUrl: './public-apis.component.html',
  styleUrls: ['./public-apis.component.scss']
})
export class PublicApisComponent {
  disneyCharactersData: any = null;
  digitalOceanStatusData: any = null;
  bankHolidaysData: { [key: string]: DivisionData } | null = null;
  
  loading: { [key: string]: boolean } = {
    disneyCharacters: false,
    digitalOceanStatus: false,
    bankHolidays: false
  };
  
  error: { [key: string]: string } = {
    disneyCharacters: '',
    digitalOceanStatus: '',
    bankHolidays: ''
  };

  // Track collapsed state for each API response
  collapsed: { [key: string]: boolean } = {
    disneyCharacters: false,
    digitalOceanStatus: false,
    bankHolidays: false
  };

  // Info section collapsible state
  infoSectionExpanded = false;

  constructor(private readonly publicApiService: PublicApiService) {}

  getDisneyCharacters(): void {
    this.loading['disneyCharacters'] = true;
    this.error['disneyCharacters'] = '';
    this.disneyCharactersData = null;
    
    this.publicApiService.getDisneyCharacters().subscribe({
      next: (data) => {
        this.disneyCharactersData = data;
        this.loading['disneyCharacters'] = false;
      },
      error: (err) => {
        this.error['disneyCharacters'] = 'Error fetching Disney characters data: ' + err.message;
        this.loading['disneyCharacters'] = false;
      }
    });
  }

  getDigitalOceanStatus(): void {
    this.loading['digitalOceanStatus'] = true;
    this.error['digitalOceanStatus'] = '';
    this.digitalOceanStatusData = null;
    
    this.publicApiService.getDigitalOceanStatus().subscribe({
      next: (data) => {
        this.digitalOceanStatusData = data;
        this.loading['digitalOceanStatus'] = false;
      },
      error: (err) => {
        this.error['digitalOceanStatus'] = 'Error fetching Digital Ocean status data: ' + err.message;
        this.loading['digitalOceanStatus'] = false;
      }
    });
  }

  getBankHolidays(): void {
    this.loading['bankHolidays'] = true;
    this.error['bankHolidays'] = '';
    this.bankHolidaysData = null;
    
    this.publicApiService.getBankHolidays().subscribe({
      next: (data) => {
        this.bankHolidaysData = data;
        this.loading['bankHolidays'] = false;
      },
      error: (err) => {
        this.error['bankHolidays'] = 'Error fetching bank holidays data: ' + err.message;
        this.loading['bankHolidays'] = false;
      }
    });
  }
  
  getRegions(): string[] {
    if (!this.bankHolidaysData) {
      return [];
    }
    return Object.keys(this.bankHolidaysData);
  }

  // Clear methods for each API
  clearDisneyCharacters(): void {
    this.disneyCharactersData = null;
    this.error['disneyCharacters'] = '';
    this.collapsed['disneyCharacters'] = false;
  }

  clearDigitalOceanStatus(): void {
    this.digitalOceanStatusData = null;
    this.error['digitalOceanStatus'] = '';
    this.collapsed['digitalOceanStatus'] = false;
  }

  clearBankHolidays(): void {
    this.bankHolidaysData = null;
    this.error['bankHolidays'] = '';
    this.collapsed['bankHolidays'] = false;
  }

  // Toggle collapse state
  toggleCollapse(apiName: string): void {
    this.collapsed[apiName] = !this.collapsed[apiName];
  }

  // Toggle info section
  toggleInfoSection(): void {
    this.infoSectionExpanded = !this.infoSectionExpanded;
  }
}
