
(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('ChargementTransfertController', ChargementTransfertController);


    ChargementTransfertController.$inject = ['$rootScope', '$state', '$scope', 'User', '$uibModal', 'Principal', 'Notification', 'ChargementTransfert', 'LigneChargementTransfert', 'Client', 'MotifOperation', 'Nationalite', 'Utils', 'Upload', '$timeout'];

    function ChargementTransfertController($rootScope, $state, $scope, User, $uibModal, Principal, Notification, ChargementTransfert, LigneChargementTransfert, Client, MotifOperation, Nationalite, Utils, Upload, $timeout) {
        var vm = this;

        vm.chargementTransferts = [];
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
        vm.getCompte = getCompte;
        vm.getChrgClient = getChrgClient;
        vm.nationalites = Nationalite.queryAll();
        vm.motifOperation = MotifOperation.query();
        vm.chargementSelect = null;
        vm.page = 1;
        vm.page2 = 1;
        vm.totalItems = 0;
        vm.totalItems2 = 0;
        vm.itemsPerPage = 20;
        vm.queryCount = 0;
        vm.queryCount2 = 0;
        vm.format = null;
        vm.filtrerParStatut = filtrerParStatut;
        vm.imprimerSelonStatut = imprimerSelonStatut;

        vm.urlChrg = 'api/etat/chargement-transfert/etat/rapportChargement/';
        vm.urlChrgByStatut = 'api/etat/chargement-transfert/etat/rapportChargementSelonStatut'


        function filtrerParStatut(id) {
            LigneChargementTransfert.queryDetailByStatut({
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

        $rootScope.$on('ptiApp:chargementEnmasse', function (event, data) {
            loadAll();
        });

        console.log('state', $state.current.name);
        //vm.charge= charge;
        loadAll();
        Client.queryBytypePer(function (result) {
            console.log('data', result);
            vm.entrepriseMorale = result;
        });
        function getCompte(id) {
            vm.ctrlTabC = true;
            vm.stckId = id;
            Client.findCompte({id: id}, function (result) {
                console.log(result);
                vm.cptEntrp = result;
            });
            ChargementTransfert.chargementByClient({
                id: id,
                page: vm.page2 - 1,
                size: vm.itemsPerPage

            }, function (result, headers) {
                console.log(result);
                vm.totalItems2 = headers('X-Total-Count');
                vm.queryCount2 = result.length;
                console.log('transaction', result);
                vm.canShowResultCount2 = true;
                vm.chargementTransferts = result;
            });
        }
        function clear(picFile) {
            $scope.picFile = null;
            vm.progressionBar = 0;
            vm.fileName = "";
            vm.ctrlbtnsave = true;
        }
        function loadAll() {

            if ($state.current.name === 'chargement-transfert-valide') {
                vm.afficheFichier = true;

            } else {
                vm.afficheFichier = false;
                vm.afficherOption = true;

            }

        }
        function getChrgClient() {
            ChargementTransfert.chargementByClient({id: vm.stckId}, function (result) {
                console.log(result);
                vm.chargementTransferts = result;
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
                LigneChargementTransfert.findById({id: vm.defchargement.id}, function (data) {
                    data = angular.fromJson(data);
                    console.log(data.values);
                    vm.defchargement = charg;
                    loadAll();
                    getChargement(charg, index);
                });
            }
            //     ChargementTransfert.delete({id:  vm.defchargement.id},function(data){
            //         loadAll();
            //              })
            // }
        }
        function getChargement(chargement, index) {
            console.log('val', chargement.statut);
            console.log('id', vm.clientEtprId, chargement);
            vm.click = false;
            vm.defchargement = chargement;

            vm.selectedRow = index;
            if ($state.current.name === 'chargement-transfert-valide') {
                vm.afficheFichier = true;
            } else {
                vm.afficheFichier = false;
            }

            // LigneChargementTransfert.queryDetail({
            //     id: chargement.id,
            //     page:vm.page-1,
            //     size:vm.itemsPerPage,
            // }, function (result,headers) {
            //     console.log('lignes',result);
            //     vm.totalItems=headers('X-Total-Count');
            //     vm.queryCount = result.length;
            //     console.log('transaction',result);
            //     vm.canShowResultCount=true;
            //     vm.ligneChargements = result;
            // });
            LigneChargementTransfert.queryDetailByStatut({
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
            getCompte(vm.stckId);
        });

        vm.selectedRow = null;

        vm.traitementOperationReglement = function () {
            console.log('il');
            Principal.identity().then(function (account) {
                vm.operation.login = account.login;
            });
            $uibModal.open({
                templateUrl: 'app/entities/operation/reglement-organisation-validation/reglement-traitement.html',
                controller: 'ChargementTransfertController',
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
                controller: 'ChargementTransfertDialogController',
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
            // var typeExel = "application/vnd.ms-excel;base64";
            // var typepdf = "application/pdf;base64";
            // ChargementTransfert.imprimer({codeChargement: idChargement}, function (result) {
            //     var pdf = new Blob([result.data], {type: typepdf});
            //     var url = window.URL.createObjectURL(pdf);
            //     window.open(url);
            // });
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
            console.log(vm.clientEtprId, vm.numeroCompte, vm.typeTransfert, vm.codePays, vm.motifOp, vm.montant);
           
            if (vm.typeTransfert !== false)
            {


                var url = 'api/upload/chargement/fichier';
                console.log(url);
                if (vm.numeroCompte !== null && vm.motifOp !== null && vm.codePays !== null && vm.montant !== null) {
                    Upload.upload({
                        url: url,
                        data: {file: file, compteDebit: vm.numeroCompte, typeTransfert: vm.typeTransfert, clientId: vm.clientEtprId, codePays: vm.codePays, motifOperation: vm.motifOp, montantEntrp: vm.montant,retraitGratuit:vm.retraitGratuit}

                    }).then(function (resp) {
                        console.log(resp);
                        loadAll();
                        if (resp.status === 200) {
                            console.log(file);
                            getChrgClient();
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
                    Notification.error("Veuillez choisir un numéro de compte,un motif,un montant ou le pays");
                }
            } else {
                Notification.error("Veuillez choisir une option de chargement");
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
