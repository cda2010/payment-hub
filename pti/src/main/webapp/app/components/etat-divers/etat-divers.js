(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('EtatDiversController', EtatDiversController);

    EtatDiversController.$inject = ['RequestUtils','$rootScope','$state', 'TableService', 'Utils','$scope'];

    function EtatDiversController(RequestUtils,$rootScope,$state,TableService, Utils, $scope) {

        var vm = this;
        vm.clear = clear;
        vm.etatChoisi = '';
        vm.getEtatChoisi = getEtatChoisi;
/*        vm.etats = [
            'COMPTE EN INSTANCE',
            'COMPTE BLOQUE OU CLOTURE',
            'BENEFICIAIRE',
            'COMPTE INACTIF',
            'TRANSACTION PENDING',
            'COMPTE USER',
            'CONNEXION DENIED',
           ' PROFIL USER',
           ' RAPPORT USER ACTIVITY',
            'MODIFICATION PROFIL'
        ];*/

        vm.etats = [
            {
                id:'COMPTE EN INSTANCE',
                libelle: 'Comptes en instance'
            },
            {
                id:'COMPTE BLOQUE OU CLOTURE',
                libelle: 'Comptes bloqués ou clôturés'
            },
            {
                id:'PROFIL USER',
                libelle: 'Profils utilisateurs'
            },
            /*{
                id:'PROFIL HABILITATION',
                libelle: 'Profils et habilitations'
            },*/
            {
                id:'CONNEXION DENIED',
                libelle: 'Connexions echouées'
            },
            {
                id:'COMPTE USER',
                libelle: 'Comptes de connexion des utilisateur'
            },
            {
                id:'TRANSACTION PENDING',
                libelle: 'Transactions en instance'
            },
            {
                id:'COMPTE INACTIF',
                libelle: 'Comptes inactifs depuis 6 mois'
            },
            {
                id:'BENEFICIAIRE',
                libelle: 'Bénéficiaires d\'un client'
            },
            {
                id:'RAPPORT USER ACTIVITY',
                libelle: 'Rapport d\'activité des utilisateurs'
            },
            {
                id:'MODIFICATION PROFIL',
                libelle: 'Modifications sur les profils utilisateurs'
            },
            {
                id: 'JOURNAL TRANSACTION',
                libelle: 'Journal des transactions d\'une date donnée'
            },
            {
                id: 'POINTS DE VENTE INACTIFS',
                libelle: 'Points de vente inactifs sur 30 jours'
            },
            {
                id: 'POINTS DE VENTE NON ACTIVES',
                libelle: 'Points de vente non activés'
            }

        ];

        vm.item = {
            typeEtat : null
        };

        vm.hasError = hasError;

        function hasError(form, field) {
            return (form.$submitted || field.$touched) && field.$invalid;
        }

        function getEtatChoisi() {
            console.log(vm.etatChoisi);
            if (vm.etatChoisi === "COMPTE EN INSTANCE")
                $state.go('compte-en-instance');
            else if (vm.etatChoisi === "COMPTE BLOQUE OU CLOTURE")
                $state.go('compte-bloque-cloture');
            else if (vm.etatChoisi === "BENEFICIAIRE")
                $state.go('etat-beneficiaire');
            else if (vm.etatChoisi === "COMPTE INACTIF")
                $state.go('compte-inactif');
            else if (vm.etatChoisi === "TRANSACTION PENDING")
                $state.go('transaction-pending');
            else if (vm.etatChoisi === "COMPTE USER")
                $state.go('etat-list-user');
            else if (vm.etatChoisi === "PROFIL USER")
                $state.go('profil-user');
           /* else if (vm.etatChoisi === "PROFIL HABILITATION")
                $state.go('profil-habilitation');*/
            else if (vm.etatChoisi === "CONNEXION DENIED")
                $state.go('connexion-denied');
            else if (vm.etatChoisi === "RAPPORT USER ACTIVITY")
                $state.go('rapport-activite-user');
            else if (vm.etatChoisi === "MODIFICATION PROFIL")
                $state.go('audit-sur-les-profils');
            else if (vm.etatChoisi === "JOURNAL TRANSACTION")
                $state.go('journal-transaction');
            else if (vm.etatChoisi === "POINTS DE VENTE INACTIFS")
                $state.go('point-vente-inactif');
            else if (vm.etatChoisi === "POINTS DE VENTE NON ACTIVES")
                $state.go('point-vente-non-active');

        }

        function clear() {
            vm.item = {
                typeEtat : null,
            };
        }
    }

})();
