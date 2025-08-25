import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator';
import { Card, CardHeader, CardContent, CardFooter } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import {
  Car,
  Camera,
  BatteryCharging,
  Wrench,
  Fuel,
  Siren,
  MapPin,
  Home,
  ParkingCircle,
  Route,
  CheckCircle2,
  Loader,
  Circle,
  Phone,
  MessageSquare,
  CircleHelp
} from 'lucide-react';

// Shadcn UI components
const components = {
  Button,
  Textarea,
  Input,
  Separator,
  Card,
  CardHeader,
  CardContent,
  CardFooter,
  Badge,
  Avatar,
  AvatarFallback,
};

// Icon components
const icons = {
  Car,
  Camera,
  BatteryCharging,
  Wrench,
  Fuel,
  Siren,
  MapPin,
  Home,
  ParkingCircle,
  Route,
  CheckCircle2,
  Loader,
  Circle,
  Phone,
  MessageSquare,
  CircleHelp
};

const ActiveRequestCard = ({ requestDetails, onCancel }) => {
  const [currentStep, setCurrentStep] = useState(1); // 0: Demande envoyée, 1: Confirmé par le dépanneur...

  const timelineSteps = [
    { id: 0, label: 'Demande envoyée' },
    { id: 1, label: 'Confirmé par le dépanneur' },
    { id: 2, label: 'Dépanneur en route' },
    { id: 3, label: 'Intervention terminée' },
  ];

  const getStatusIcon = (stepId) => {
    if (stepId < currentStep) {
      return <icons.CheckCircle2 className="h-6 w-6 text-green-500" />;
    } else if (stepId === currentStep) {
      return <icons.Loader className="h-6 w-6 text-blue-500 animate-spin" />;
    } else {
      return <icons.Circle className="h-6 w-6 text-gray-300" />;
    }
  };

  return (
    <Card className="w-full max-w-2xl rounded-3xl shadow-2xl">
      <CardHeader className="text-center space-y-2">
        <h1 className="text-3xl sm:text-4xl font-extrabold text-gray-800">Suivi de votre demande</h1>
        <p className="text-sm text-gray-500">ID de la demande: #A4B7-89C1</p>
      </CardHeader>

      <Separator />

      <CardContent className="space-y-6 p-4 sm:p-6">
        {/* Dépanneur info */}
        <div className="flex items-center space-x-4 p-4 rounded-xl bg-blue-50/50 border border-blue-100">
          <Avatar className="h-12 w-12 text-xl bg-blue-200 text-blue-800 font-bold">
            <AvatarFallback>GD</AvatarFallback>
          </Avatar>
          <div className="flex-1 space-y-1">
            <h3 className="text-lg font-bold">Garage Dubois</h3>
            <p className="text-sm text-gray-600">Arrivée estimée : 12 minutes</p>
          </div>
          <Badge className="bg-orange-500 text-white font-semibold">En attente</Badge>
        </div>

        {/* Timeline */}
        <div className="relative pl-6">
          <div className="absolute top-0 bottom-0 left-[11px] w-0.5 bg-gray-200" />
          {timelineSteps.map((step) => (
            <div key={step.id} className="relative mb-6 flex items-center">
              <div className="absolute left-0 top-0 -translate-x-1/2 -translate-y-[2px]">
                {getStatusIcon(step.id)}
              </div>
              <span className="ml-8 text-md font-semibold text-gray-700">{step.label}</span>
            </div>
          ))}
        </div>

        {/* Récapitulatif de l'intervention */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold text-gray-700">Récapitulatif de l'intervention</h2>
          <div className="space-y-2 text-gray-600 text-sm">
            <div className="flex items-center gap-2">
              <icons.Car className="h-6 w-6 text-gray-500" />
              <span>Votre véhicule: <span className="font-semibold">{requestDetails.vehicleInfo || 'Non spécifié'}</span></span>
            </div>
            <div className="flex items-center gap-2">
              <icons.MapPin className="h-6 w-6 text-gray-500" />
              <span>Lieu: <span className="font-semibold">{requestDetails.location || 'Non spécifié'}</span></span>
            </div>
            <div className="flex items-center gap-2">
              <icons.CircleHelp className="h-6 w-6 text-gray-500" />
              <span>Problème signalé: <span className="font-semibold">{requestDetails.breakdownType}</span></span>
            </div>
            {requestDetails.breakdownType === 'Autre' && requestDetails.breakdownDescription && (
              <div className="flex items-start gap-2">
                <icons.MessageSquare className="h-6 w-6 text-gray-500 mt-1" />
                <span className="italic flex-1">"{requestDetails.breakdownDescription}"</span>
              </div>
            )}
            <div className="flex items-center gap-2">
              <icons.Camera className="h-6 w-6 text-gray-500" />
              <span>Photo fournie: <span className="font-semibold">{requestDetails.isPhotoAdded ? 'Oui' : 'Non'}</span></span>
            </div>
          </div>
        </div>
      </CardContent>

      <CardFooter className="flex flex-col sm:flex-row gap-2 justify-between pt-4">
        <Button className="w-full px-4 py-2 font-semibold rounded-full bg-blue-600 hover:bg-blue-700 text-base">
          <icons.Phone className="h-4 w-4 mr-2" />
          Appeler le dépanneur
        </Button>
        <Button className="w-full px-4 py-2 font-semibold rounded-full bg-blue-600 hover:bg-blue-700 text-base">
          <icons.MessageSquare className="h-4 w-4 mr-2" />
          Envoyer un message
        </Button>
        <Button
          className="w-full px-4 py-2 font-semibold rounded-full text-base"
          variant="destructive"
          onClick={onCancel}
        >
          Annuler la demande
        </Button>
      </CardFooter>
    </Card>
  );
};

const App = () => {
  const [uiState, setUiState] = useState('form'); // 'form' or 'tracking'
  const [breakdownType, setBreakdownType] = useState(null);
  const [breakdownDescription, setBreakdownDescription] = useState('');
  const [vehicleInfo, setVehicleInfo] = useState('');
  const [location, setLocation] = useState(null);
  const [isPhotoAdded, setIsPhotoAdded] = useState(false);
  const [vehicleInfoError, setVehicleInfoError] = useState(false);

  // Determine if the form is valid to enable the "Continuer" button
  const isFormValid = breakdownType && (breakdownType !== 'Autre' || breakdownDescription.trim() !== '');

  const breakdownOptions = [
    { type: 'Batterie', icon: icons.BatteryCharging },
    { type: 'Moteur', icon: icons.Wrench },
    { type: 'Pneu', icon: icons.Car },
    { type: 'Essence', icon: icons.Fuel },
    { type: 'Autre', icon: icons.Siren },
  ];

  const locationOptions = [
    { type: 'Route', icon: icons.Route },
    { type: 'Parking', icon: icons.ParkingCircle },
    { type: 'Domicile', icon: icons.Home },
  ];

  const handlePhotoClick = () => {
    setIsPhotoAdded(!isPhotoAdded);
  };

  const handleContinueClick = () => {
    if (vehicleInfo.trim() === '') {
      setVehicleInfoError(true);
      return;
    }
    setVehicleInfoError(false);

    // Collect form data and transition to tracking view
    const requestDetails = { breakdownType, breakdownDescription, vehicleInfo, location, isPhotoAdded };
    console.log("Form data submitted:", requestDetails);
    setUiState('tracking');
  };

  const handleCancelRequest = () => {
    // Reset state and return to the form view
    setBreakdownType(null);
    setBreakdownDescription('');
    setVehicleInfo('');
    setLocation(null);
    setIsPhotoAdded(false);
    setVehicleInfoError(false);
    setUiState('form');
  };

  if (uiState === 'tracking') {
    return (
      <div className="flex flex-col items-center justify-center p-4 sm:p-8 bg-gray-100 min-h-screen font-sans antialiased">
        <ActiveRequestCard
          requestDetails={{ breakdownType, breakdownDescription, vehicleInfo, location, isPhotoAdded }}
          onCancel={handleCancelRequest}
        />
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-start p-4 bg-gray-100 min-h-screen font-sans antialiased overflow-y-auto">
      <div className="w-full max-w-lg bg-white p-6 sm:p-8 rounded-3xl shadow-2xl space-y-6">
        <h1 className="text-3xl font-extrabold text-center text-gray-800">Quel est le problème ?</h1>
        <p className="text-center text-sm text-gray-500 -mt-4">Donnez des détails pour une meilleure prise en charge.</p>

        <Separator />

        {/* Type de panne */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold text-gray-700">Type de panne</h2>
          <div className="grid grid-cols-2 gap-3">
            {breakdownOptions.map((option) => (
              <Button
                key={option.type}
                variant="outline"
                className={`flex flex-col items-center justify-center h-28 text-center rounded-2xl transition-all duration-200 shadow-lg border-2 ${breakdownType === option.type ? 'bg-blue-600 text-white border-blue-600 scale-105' : 'bg-gray-50 text-gray-600 hover:bg-gray-100 border-gray-200'}`}
                onClick={() => setBreakdownType(option.type)}
              >
                <option.icon className={`h-10 w-10 mb-2 ${breakdownType === option.type ? 'text-white' : 'text-blue-600'}`} />
                <span className={`text-sm font-semibold whitespace-nowrap ${breakdownType === option.type ? 'text-white' : 'text-gray-600'}`}>{option.type}</span>
              </Button>
            ))}
          </div>
        </div>

        <Separator />

        {/* Description du problème (Conditionnel) */}
        {breakdownType === 'Autre' && (
          <div className="space-y-4">
            <h2 className="text-xl font-bold text-gray-700">Description du problème</h2>
            <Textarea
              placeholder="Veuillez décrire votre problème en détail..."
              value={breakdownDescription}
              onChange={(e) => setBreakdownDescription(e.target.value)}
              className="min-h-[120px] rounded-xl border-gray-300 focus:border-blue-500 focus:ring-blue-500"
            />
          </div>
        )}

        {/* Marque et modèle du véhicule */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold text-gray-700">Marque et modèle du véhicule</h2>
          <Input
            placeholder="Ex : Tesla Model 3"
            value={vehicleInfo}
            onChange={(e) => {
              setVehicleInfo(e.target.value);
              if (vehicleInfoError) {
                setVehicleInfoError(false);
              }
            }}
            className={`rounded-xl border-gray-300 focus:border-blue-500 focus:ring-blue-500 ${vehicleInfoError ? 'border-red-500' : ''}`}
          />
          {vehicleInfoError && (
            <p className="text-red-500 text-sm mt-1">Veuillez entrer le modèle de votre véhicule.</p>
          )}
        </div>

        <Separator />

        {/* Lieu de l'incident */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold text-gray-700">Lieu de l'incident</h2>
          <div className="grid grid-cols-3 gap-3">
            {locationOptions.map((option) => (
              <Button
                key={option.type}
                variant="outline"
                className={`flex flex-col items-center justify-center h-28 text-center rounded-2xl transition-all duration-200 shadow-lg border-2 ${location === option.type ? 'bg-blue-600 text-white border-blue-600 scale-105' : 'bg-gray-50 text-gray-600 hover:bg-gray-100 border-gray-200'}`}
                onClick={() => setLocation(option.type)}
              >
                <option.icon className={`h-10 w-10 mb-2 ${location === option.type ? 'text-white' : 'text-blue-600'}`} />
                <span className={`text-sm font-semibold whitespace-nowrap ${location === option.type ? 'text-white' : 'text-gray-600'}`}>{option.type}</span>
              </Button>
            ))}
          </div>
        </div>

        <Separator />

        {/* Bouton pour ajouter une photo */}
        <div className="flex justify-center">
          <Button
            className={`w-full flex items-center gap-2 px-6 py-3 rounded-full text-lg font-bold transition-all duration-300 ${isPhotoAdded ? 'bg-green-500 hover:bg-green-600' : 'bg-gray-700 hover:bg-gray-800'}`}
            onClick={handlePhotoClick}
          >
            {isPhotoAdded ? (
              <>
                <icons.CheckCircle2 className="h-5 w-5" />
                Photo ajoutée ✓
              </>
            ) : (
              <>
                <icons.Camera className="h-5 w-5" />
                Ajouter une photo
              </>
            )}
          </Button>
        </div>

        {/* Bouton Continuer (Pied de page) */}
        <div className="pt-4">
          <Button
            className="w-full py-6 text-lg font-bold rounded-2xl transition-all duration-200"
            disabled={!isFormValid}
            onClick={handleContinueClick}
          >
            Continuer
          </Button>
        </div>
      </div>
    </div>
  );
};

export default App;
