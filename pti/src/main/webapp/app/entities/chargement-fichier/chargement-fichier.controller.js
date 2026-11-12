
(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementFichierController', ChargementFichierController);


    ChargementFichierController.$inject = ['$rootScope', '$state', '$scope', 'User', '$uibModal', 'Principal', 'Notification', 'ChargementFichier', 'LigneFichier', 'UniteOrganisation','GroupeProfilActeur','ServiceProfil','Utils', 'Upload', '$timeout'];

    function ChargementFichierController($rootScope, $state, $scope, User, $uibModal, Principal, Notification, ChargementFichier, LigneFichier, UniteOrganisation,GroupeProfilActeur,ServiceProfil,Utils, Upload, $timeout) {
        var vm = this;

        vm.chargementFichiers = [];
        vm.organisations = [];
        vm.progressingBarAzero = progressingBarAzero;
        vm.lireFichier = lireFichier;
        vm.supprimerFichier = supprimerFichier;
        vm.click = false;
        vm.isPrint = false;
        vm.typeTransfert = false;
        vm.ctrlTabC = false;
        vm.getChargement = getChargement;
        vm.validerChrgmt = validerChrgmt;
        vm.afficheFichier = false;
        vm.afficherOption = false;
        vm.openModal = openModal;
        vm.imprimerRapport = imprimerRapport;
        vm.clear = clear;
        vm.ctrlbtnsave = true;
        vm.clientEtprId = null;
        vm.typeP = 'MORALE';
        vm.codePays = null;
        vm.motifOp = null;
        vm.stckId = null;
        vm.getOrganisation = getOrganisation;
        vm.getOrganisationByType=getOrganisationByType;
        vm.getChrgOrg = getChrgOrg;
        vm.chargementSelect = null;
        vm.filterStatut="";
        vm.page = 1;
        vm.page2 = 1;
        vm.totalItems = 0;
        vm.totalItems2 = 0;
        vm.itemsPerPage = 20;
        vm.queryCount = 0;
        vm.queryCount2 = 0;
        vm.format = null;
        vm.profilId=null;
        vm.serviceId=null;
        vm.groupeProfils=GroupeProfilActeur.queryByType({acteur:'ORGANISATION'});
        vm.seviceProfiles=ServiceProfil.queryByType({type:'ORGANISATION'});
        vm.filtrerParStatut = filtrerParStatut;
        vm.imprimerSelonStatut = imprimerSelonStatut;

        vm.urlChrg = 'api/etat/chargement-fichier/etat/rapportChargement/';
        vm.urlChrgByStatut = 'api/etat/chargement-fichier/etat/rapportChargementSelonStatut'


        function filtrerParStatut(id) {
            LigneFichier.queryDetailByStatut({
                id: id,
                page: vm.page - 1,
                size: vm.itemsPerPage,
                statut: vm.filterStatut
            }, function (result, headers) {
                console.log('lignes', result);
                vm.totalItems = headers('X-Total-Count');
                vm.queryCount = result.length;
                console.log('transaction', result);
                vm.canShowResultCount = true;
                vm.ligneChargements = result;
            });

        }

        $rootScope.$on('ptiApp:chargementFichierEnmasse', function (event, data) {
            loadAll();
        });

        console.log('state', $state.current.name);
        //vm.charge= charge;
        loadAll();
        function getOrganisation(id) {
            vm.ctrlTabC = true;
            vm.stckId = id;
            console.log('id',id);
            if(id!==null){
                ChargementFichier.chargementByOrganisation({
                    id: id,
                    page: vm.page2 - 1,
                    size: vm.itemsPerPage

                }, function (result, headers) {
                    console.log(result);
                    vm.totalItems2 = headers('X-Total-Count');
                    vm.queryCount2 = result.length;
                    console.log('transaction', result);
                    vm.canShowResultCount2 = true;
                    vm.chargementFichiers = result;
                });
            }

        }

        function getOrganisationByType(typeUo){
            console.log('typeUo----',typeUo);
            if(typeUo==='MARCHAND'){
                UniteOrganisation.queryByType({typeId:'AGENCE'},function (result) {
                    vm.organisations=result;
                    console.log('result----',result);
                })
            } else if (typeUo==='SOUS-DISTRIBUTEUR'){
                UniteOrganisation.queryByType({typeId:'MASTER'},function (result) {
                    vm.organisations=result;
                })
            }
        }
        function clear(picFile) {
            $scope.picFile = null;
            vm.progressionBar = 0;
            vm.fileName = "";
            vm.ctrlbtnsave = true;
        }
        function loadAll() {

            if ($state.current.name === 'chargement-fichier-valide') {
                vm.afficheFichier = true;

            } else {
                vm.afficheFichier = false;
                vm.afficherOption = true;

            }

        }
        function getChrgOrg() {
            ChargementFichier.chargementByOrganisation({id: vm.stckId}, function (result) {
                console.log(result);
                vm.chargementFichiers = result;
            });
        }

        vm.selectedRow = null;
        function progressingBarAzero(file) {
            vm.progressionBar = 0;
            vm.fileName = file.name;
            vm.fil = file;
            vm.ctrlbtnsave = false;
            // console.log(vm.file);
        }
        function supprimerFichier() {
            var charg = "";
            if (confirm('Voulez vous supprimer ce fichier'))
            {
                confirm('Service non disponible')
/*                LigneFichier.findById({id: vm.defchargement.id}, function (data) {
                    data = angular.fromJson(data);
                    console.log(data.values);
                    vm.defchargement = charg;
                    loadAll();
                    getChargement(charg, index);
                });*/
            }
        }
        function getChargement(chargement, index) {
            console.log('val', chargement);
            console.log('statut', vm.filterStatut);
            vm.click = false;
            vm.defchargement = chargement;

            vm.selectedRow = index;
            if ($state.current.name === 'chargement-fichier-valide') {
                vm.afficheFichier = true;
            } else {
                vm.afficheFichier = false;
            }
                LigneFichier.queryDetailByStatut({
                    id: chargement.id,
                    page: vm.page - 1,
                    size: vm.itemsPerPage,
                    statut: vm.filterStatut
                }, function (result, headers) {
                    console.log('lignes', result);
                    vm.totalItems = headers('X-Total-Count');
                    vm.queryCount = result.length;
                    console.log('transaction', result);
                    vm.canShowResultCount = true;
                    vm.ligneChargements = result;
                });
        }

        $scope.$watch("vm.page", function (id) {
            getChargement(vm.defchargement, vm.selectedRow);
        });
        $scope.$watch("vm.page2", function (id) {
            console.log('vm.stckId', vm.stckId);
           // getCompte(vm.stckId);
        });

        vm.selectedRow = null;

        vm.traitementOperationReglement = function () {
            console.log('il');
            Principal.identity().then(function (account) {
                vm.operation.login = account.login;
            });
            $uibModal.open({
                templateUrl: 'app/entities/operation/reglement-organisation-validation/reglement-traitement.html',
                controller: 'ChargementFichiertController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                // $uibModalInstance.close(true);
            });
        };
        function validerChrgmt(chargement) {
            console.log('chrgmt', chargement.statut);
            vm.operation = chargement;
            Principal.identity().then(function (account) {
                console.log('true', account);
                vm.operation.login = account.login;
            });
            $uibModal.open({
                templateUrl: 'app/entities/operation/reglement-organisation-validation/reglement-traitement.html',
                controller: 'ChargementFichierDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                //$uibModalInstance.close(true);
            });
        }

        function openModal(ligneChargement) {
            $uibModal.open({
                templateUrl: 'app/entities/chargement-transfert/chargement-solde/chargement-transfert-solde.html',
                controller: 'ChargementModalSoldeController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return  ligneChargement;
                    }
                }
            });

        }
        vm.item = {
            idChargement: null,
            statut: null,
            format: null
        };
        function imprimerRapport(idChargement) {
            vm.item.idChargement = idChargement;

            console.log('format', vm.format);
            if (vm.format === null || vm.format === '') {
                console.log('f', vm.format);
                vm.item.format = 'PDF';
            } else {
                vm.item.format = vm.format;
            }
            if (vm.item.format === 'PDF' || vm.item.format === null)
            {
                Utils.postPrintUrl(vm.urlChrg, vm.item);
            } else
            {
                if (vm.item.format === 'XLSX') {
                    vm.name = 'TransfertMasse.xlsx';
                }
                if (vm.item.format === 'CSV') {
                    vm.name = 'TransfertMasse.csv';
                }
                Utils.downloadFile(vm.urlChrg, vm.item, vm.name);
            }

        }
        function lireFichier(file) {
                console.log(vm.organisationId, vm.typeUO);
                var url = 'api/upload/chargement_fichier/fichier';
                console.log(url);
                if (vm.organisationId !== null && vm.typeUO !== null) {
                    Upload.upload({
                        url: url,
                        data: {file: file, organisationId: vm.organisationId, typeUO: vm.typeUO,profilId:vm.profilId,serviceId:vm.serviceId}

                    }).then(function (resp) {
                        console.log(resp);
                        loadAll();
                        if (resp.status === 200) {
                            console.log(file);
                            getChrgOrg();
                            $timeout(function () {
                                Notification.success('Fichier chargé sur le système avec succès');
                            }, 200);

                        } else {
                            var message = resp.header('X-partnerApp-alert');
                            Notification.success(message);
                        }
                    }, function (resp) {
                        console.log(resp);
                        Notification.error('Erreur de chargement du fichier');
                        // toastr.error(resp.status.Text, 'erreur de chargement du fichier');
                    }, function (evt) {
                        var progressPercentage = parseInt(100.0 * evt.loaded / evt.total);
                        vm.progressionBar = progressPercentage;
                    })
                } else {
                    Notification.error("Veuillez sélectionner les informations demandées");
                }
        }

        function imprimerSelonStatut(idChargement) {
            console.log("-----------------------------{}", idChargement);
            vm.item.idChargement = idChargement;
            vm.item.statut = vm.filterStatut;

            console.log('format', vm.format);
            if (vm.format === null || vm.format === '') {
                console.log('f', vm.format);
                vm.item.format = 'PDF';
            } else {
                vm.item.format = vm.format;
            }
            if (vm.item.format === 'PDF' || vm.item.format === null)
            {
                Utils.postPrintUrl(vm.urlChrgByStatut, vm.item);
            } else
            {
                if (vm.filterStatut === "RJCT") {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'RapportDeRejet.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'RapportDeRejet.csv';
                    }
                } else {
                    if (vm.item.format === 'XLSX') {
                        vm.name = 'RapportDeChargement.xlsx';
                    }
                    if (vm.item.format === 'CSV') {
                        vm.name = 'RapportDeChargement.csv';
                    }
                }

                Utils.downloadFile(vm.urlChrgByStatut, vm.item, vm.name);
            }

        }
    }
})();
