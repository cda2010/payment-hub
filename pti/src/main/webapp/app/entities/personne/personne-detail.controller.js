(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('PersonneDetailController', PersonneDetailController);

    PersonneDetailController.$inject = ['$uibModal', '$scope', '$state', 'Compte', 'DataUtils', 'CancelAlert', 'Nationalite', 'ClientByTypePersonne', 'BrancheActivite', 'TypePiece', 'RequestUtils', '$uibModalInstance', 'entity', 'BASE_URL_API', 'DateUtils', 'Utils', 'ProfilActeur', 'Notification', 'Personne', 'UniteOrganisation', 'TransactionComptePti', 'EvenementOperation', 'TableService', 'UserControls'];

    function PersonneDetailController($uibModal, $scope, $state, Compte, DataUtils, CancelAlert, Nationalite, ClientByTypePersonne, BrancheActivite, TypePiece, RequestUtils, $uibModalInstance, entity, BASE_URL_API, DateUtils, Utils, ProfilActeur, Notification, Personne, UniteOrganisation, TransactionComptePti, EvenementOperation, UserControls) {
        var vm = this;
        var URL = BASE_URL_API + 'etat/export/transactionOfCompteAutre';
        vm.client = entity;

        console.log('vm.client ok', vm.client.documentDTOS);
        console.log('vm.client', vm.client);
        console.log('vm.compteDTOS', vm.client.compteDTOS.length);
        vm.tail = vm.client.compteDTOS.length;
        vm.createCompteBanque = createCompteBanque;
        $scope.modal_instance = $uibModalInstance;
        vm.pieces = [];
        vm.profilActeurs = ProfilActeur.queryProfileClient({acteur: 'CLIENT'});
        vm.typepieces = TypePiece.query();
        vm.branchesActivites = BrancheActivite.query();
        vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;
        vm.comptesAll = [];
        vm.comptesBanques = [];
        vm.comptesCartes = [];
        vm.transactions = [];
        vm.evenementsOperation = [];
        vm.auditEvent = [];
        vm.userControl = [];
        vm.entite = "Personne";
        vm.nationalites = Nationalite.query();
        vm.confirmReset = confirmReset;
        vm.resetPassword = resetPassword;
        vm.getDetailTransaction = getDetailTransaction;
        vm.getDetailEvenement = getDetailEvenement;
        vm.changeStatutClient = changeStatutClient;
        vm.changeProfileCient = changeProfileCient;
        vm.identifierClient = identifierClient;
        vm.curentCompteAutred = null;
        vm.canShowResultCount = false;
        vm.canShowResultCount2 = false;
        vm.canShowResultCount3 = false;
        vm.canShowResultCount5 = false;
        vm.exporterTransClient = exporterTransClient;
        vm.leverEvenement = leverEvenement;
        vm.activefiltre = activefiltre;
        vm.getBilanOp = getBilanOp;
        vm.getTransactionFiltre = getTransactionFiltre;
        vm.getUserControl = getUserControl;
        vm.getTracePersonne = getTracePersonne;
        vm.ajouterCompteComo = ajouterCompteComo;
        vm.ctrlFiltre = false;
        vm.format = null;
        vm.page = 1;
        vm.page2 = 1;
        vm.page3 = 1;
        vm.page4 = 1;
        vm.page5 = 1;
        /*Nombre d'élements*/
        vm.totalItems = 0;
        vm.totalItems2 = 0;
        vm.totalItems3 = 0;
        vm.totalItems5 = 0;
        vm.itemsPerPage = 20;
        vm.queryCount = 0;

        vm.queryCount2 = 0;
        vm.queryCount3 = 0;
        vm.queryCount5 = 0;

        vm.itemSearch = {
            'compteAutreId': null,
            'format': null,
            'type': null,
            'dateDebut': null,
            'dateFin': null,
            'dateFinT': null,
            'dateDebutT': null
        };

        vm.typeTransaction = null;

        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        function activefiltre() {
            vm.ctrlFiltre = true;
        }

        function getBilanOp() {
            console.log('search--', vm.itemSearch);
            getBilanOperation(vm.personneE.id);
        }
        function getTransactionFiltre() {
            console.log('search--', vm.itemSearch);
            getDetailTransaction();
        }
        vm.initFiltre = function () {
            vm.ctrlFiltre = false;
            vm.itemSearch = {
                "dateDebut": null,
                "dateFin": null,
                "dateFinT": null,
                "dateDebutT": null,
            };
        };

        vm.openFile = function (document) {
            $uibModal.open({
                templateUrl: 'app/entities/personne/view-file.html',
                controller: 'ViewFileController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return{
                            documentId: document.id,
                            imageFolder: document.imageFolder,
                            type: document.valeurContentType,
                            valeur: document.valeur
                        }
                    }
                }
            }).result.then(function () {
            }, function () {
            });
        };


        function createCompteBanque(data) {
            console.log(data);
            console.log('alibaba ', vm.comptes[0].compteAutreDTO.codeFse);
            vm.banque = {
                client: vm.client,
                codeFse: vm.comptes[0].compteAutreDTO.codeFse
            };
            $uibModal.open({
                templateUrl: 'app/entities/personne/create-compte-banque.html',
                controller: 'CreateCompteBanqueController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return{
                            client: vm.banque
                        }
                    }
                }
            }).result.then(function () {
                console.log('Operation refresh');
                refreshCompte();
            }, function () {
            });
        }


        vm.changeEtat = function (cpt) {
            if (cpt.cpt.etatCompte === 'FERME') {
                Compte.changeEtat({id: cpt.cpt.id}, function (result) {
                    angular.forEach(vm.comptesAll, function (value, key) {
                        if (value.cpt.id == result.id) {
                            value.cpt = result;
                        }
                    })
                    Notification.success("Etat changé avec succès");
                    console.log(result);
                }, function (error) {
                    Notification.error("L'opération a échoué");

                    console.log(error);
                })
            } else {
                $uibModal.open({
                    templateUrl: 'app/entities/personne/change-etat-confirm-dialog.html',
                    controller: 'ChangeEtatConfirmController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {}
                }).result.then(function () {
                    Compte.changeEtat({id: cpt.cpt.id}, function (result) {
                        angular.forEach(vm.comptesAll, function (value, key) {
                            if (value.cpt.id == result.id) {
                                value.cpt = result;
                            }
                        });
                        Notification.success("Etat changé avec succès");
                        console.log(result);
                    }, function (error) {
                        Notification.error("L'opération a échoué");

                        console.log(error);
                    })
                }, function () {
                });
            }
        };
        vm.changeStatut = function (cpt) {
            vm.type = cpt.cpt.statutCompte;
            if (cpt.cpt.statutCompte == 'DESACTIVE') {
                Compte.changeStatut({id: cpt.cpt.id}, function (result) {
                    angular.forEach(vm.comptesAll, function (value, key) {
                        if (value.cpt.id == result.id) {
                            value.cpt = result;
                        }
                    });
                    Notification.success("Statut changé avec succès");
                    console.log(result);
                }, function (error) {
                    Notification.error("L'opération a échoué");
                    console.log(error);
                })
            } else {
                console.log("ok***", cpt.cpt.statutCompte);
                $uibModal.open({
                    templateUrl: 'app/entities/personne/change-status-confirm-dialog.html',
                    controller: 'ChangeStatutConfirmController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return vm.type;
                        }
                    }
                }).result.then(function () {
                    Compte.changeStatut({id: cpt.cpt.id}, function (result) {
                        angular.forEach(vm.comptesAll, function (value, key) {
                            if (value.cpt.id == result.id) {
                                value.cpt = result;
                            }
                        });
                        Notification.success("Statut changé avec succès");
                        console.log(result);
                    }, function (error) {
                        Notification.error("L'opération a échoué");
                        console.log(error);
                    })
                }, function () {
                });
            }
        };

        vm.changeStatutCompteBanque = function (cpt, type) {
            console.log('all', cpt, type);
            console.log('cpt.compteBanqueDTO', cpt.compteBanqueDTO);
            $uibModal.open({
                templateUrl: 'app/entities/personne/change-status-confirm-dialog.html',
                controller: 'ChangeStatutConfirmController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: function () {
                        return type;
                    }
                }
            }).result.then(function () {
                console.log('codeFse', cpt.compteBanqueDTO.codeFse);
                UniteOrganisation.queryBycodeFSE({codeFSE: cpt.compteBanqueDTO.codeFse}, function (result) {
                    console.log(result);
                    vm.fseCompteId = result.id;
                    if (type === 'DESACTIVE') {
                        desactiveCompteBanque(cpt);
                    } else if (type === 'ACTIVE') {
                        verifValideCompteBanue(cpt, vm.fseCompteId);
                        // desactiveCompteBanque(cpt);
                    } else {
                        Notification.error("L'opération a échoué");
                    }
                });
            }, function () {
            });

        };

        vm.changeStatutCompteCarte = function (cpt, type) {
          
            $uibModal.open({
                templateUrl: 'app/entities/personne/change-status-confirm-dialog.html',
                controller: 'ChangeStatutConfirmController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    entity: function () {
                        return type;
                    }
                }
            }).result.then(function () {
                UniteOrganisation.queryBycodeFSE({codeFSE: cpt.carteDTO.codeFse}, function (result) {
                    console.log(result);
                    vm.fseCompteId = result.id;
                    if (type === 'DESACTIVE') {
                        desactiveCompteBanque(cpt);
                    } else if (type === 'ACTIVE') {
                        verifValideCompteBanue(cpt, vm.fseCompteId);
                        // desactiveCompteBanque(cpt);
                    } else {
                        Notification.error("L'opération a échoué");
                    }
                });
            }, function () {
            });

        };
 function desactiveCarte(cpt) {
            Compte.changeStatutCompteBanque({id: cpt.id}, function (result) {
                angular.forEach(vm.comptesCartes, function (value, key) {
                    if (value.id === result.id) {
                        value.statutCompte = result.statutCompte;
                        console.log('ok---1')
                    }
                });
                Notification.success("Statut changé avec succès");
                console.log(result);
            }, function (error) {
                Notification.error("L'opération a échoué");
                console.log(error);
            })
        }

        function desactiveCompteBanque(cpt) {
            Compte.changeStatutCompteBanque({id: cpt.id}, function (result) {
                angular.forEach(vm.comptesBanques, function (value, key) {
                    if (value.id === result.id) {
                        value.statutCompte = result.statutCompte;
                        console.log('ok---1')
                    }
                });
                Notification.success("Statut changé avec succès");
                console.log(result);
            }, function (error) {
                Notification.error("L'opération a échoué");
                console.log(error);
            })
        }

        function verifValideCompteBanue(compte, fseId) {
            console.log('compte a verifier----------*', compte);
            if (compte.compteBanqueDTO !== null) {
                vm.agence = compte.compteBanqueDTO.codeAgence;
                vm.cle = compte.compteBanqueDTO.rib
            } else if (compte.carteDTO !== null) {
                vm.agence = compte.carteDTO.last4Digit;
                vm.cle = compte.carteDTO.last4Digit;
            }


            RequestUtils.InfoClient.get({
                compte: compte.valeurCompte,
                agence: vm.agence,
                cle: vm.cle,
                codeFse: fseId
            }, function (datas) {
                vm.personneE = datas.personneDTO;
                vm.pieceE = datas.pieceDTO;
                vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                vm.personneMoraleE = datas.personneMoraleDTO;
                console.log('view', datas);
                var message = "";
                if (vm.personneE.typePersonne == "PHYSIQUE") {
                    message += "Confirmez les informations du titulaire du compte\n" + "Nom : " + vm.personnePhysiqueE.nomClient + "\n" + " Prenom : " + vm.personnePhysiqueE.prenomClient + "\n" + "Date naissance : " + vm.personnePhysiqueE.dateNaissance + "\n " + "Doc Identification : " + vm.pieceE.typePieceLibelleTypePiece + "(" + vm.pieceE.referencePiece + ")";
                } else {
                    message += "Confirmez les informations du titulaire du compte\n" + "Raison Sociale : " + vm.personneMoraleE.raisonSociale + "\n" + " Nom Dirigeant : " + vm.personneMoraleE.dirigeant + "\n" + "Date RCCM : " + vm.personneMoraleE.dateRCCM + "\n " + "Numero IFU : " + vm.personneMoraleE.numreoIfu + "";
                }
                CancelAlert.confirm(function (arg) {
                    console.log(datas);
                    desactiveCompteBanque(compte);
                }, function (arg) {
                    // body...
                }, message, false);
            }, function () {
                Notification.error("COMPTE INTROUVABLE");
            });
        }
        function verifCptbanqueTest(cpt, fse) {

            UniteOrganisation.queryBycodeFSE({codeFSE: cpt.compteBanqueDTO.codeFse
            }, function (result) {
                console.log(result);
                vm.fseCompteId = result.id;
                var message = "";
                message += "Confirmez les informations du titulaire du compte Nom :MAIGA 226";
                CancelAlert.confirm(function (arg) {
                    console.log('clicked', result);
                    desactiveCompteBanque(cpt);
                }, function (arg) {
                    // body...
                }, message, false);
            }, function () {

            });

        }

        function  resetPassword() {
            //console.log(UserControls.query())
            if (vm.personneE.typePersonne == 'PHYSIQUE') {
                Personne.resetPassword({'telephone': vm.personneE.telephone, 'login': vm.personneE.telephone, 'codePays': vm.personneE.codePaysTelephone, typeActeur: 'CLIENT'}, function (result) {
                    Notification.success('Code PIN rénitialisé avec succès')
                    console.log(result);
                }, function (error) {
                    console.log(error);
                    Notification.error('Echec de l\'opération')
                });
            } else {
                Personne.resetPassword({'telephone': vm.personneE.telephone, login: vm.personneE.email, codePays: vm.personneE.codePaysTelephone, typeActeur: 'ENTREPRISE'}, function (result) {
                    Notification.success('Code PIN rénitialisé avec succès')
                    console.log(result);
                }, function (error) {
                    console.log(error);
                    Notification.error('Echec de l\'opération')
                });
            }
        }

        function confirmReset() {
            CancelAlert.confirm(function () {
                resetPassword()
                        .success(function (datas) {
                            console.log('Yes', datas);
                        })
                        .error(function () {
                            console.log('No', datas);
                        })
            })

        }

        function identifierClient() {
            if (!vm.client.clientIdentifie) {
                ClientByTypePersonne.identifier({id: vm.client.id}, function (result) {
                    vm.client = result;
                    vm.oldProfileId = vm.client.profilActeurId;
                    Notification.success("Client identifié avec succès")
                })
            } else {
                Notification.error("Client déjà dentifier")
            }
        }
        function  changeProfileCient() {
            if (vm.oldProfileId != vm.client.profilActeurId) {
                ClientByTypePersonne.setProfileActeur({id: vm.client.id, profileId: vm.client.profilActeurId},
                        function (result) {
                            vm.oldProfileId = vm.client.profilActeurId;
                            vm.client = result;
                            Notification.success("Profile changé avec succès")
                        })
            }
        }
        function changeStatutClient() {
            if (!vm.client.clientValide) {
                ClientByTypePersonne.changeStatut({id: vm.client.id}, function (result) {
                    vm.client = result;
                    Notification.success("Client activé avec succès")
                    // console.log(result);
                });
            }
            if (vm.client.clientValide) {
                ClientByTypePersonne.changeStatut({id: vm.client.id}, function (result) {
                    vm.client = result;
                    Notification.success("Client désactivé avec succès")
                    // console.log(result);
                });
            }
        }

        function getDetailTransaction() {
            console.log('vm.itemSearch', vm.itemSearch.dateDebutT);
            if (vm.curentCompteAutred > 0) {
                TransactionComptePti.queryByCompteAutreOptimise({
                    compteAutreId: vm.curentCompteAutred,
                    dateDebut: vm.itemSearch.dateDebutT,
                    dateFin: vm.itemSearch.dateFinT,
                    jhiType: vm.typeTransaction,
                    page: vm.page - 1,
                    size: vm.itemsPerPage
                }, function (result, headers) {
                    vm.transactions = result;
                    vm.totalItems = headers('X-Total-Count');
                    vm.queryCount = result.length;
                    console.log('transaction', result);
                    vm.canShowResultCount = true;
                })
            }
        }

        function getDetailEvenement() {
            if (vm.curentCompteAutred > 0) {
                vm.estLevee = 0;
                EvenementOperation.queryByCompteAutre({
                    compteAutreId: vm.curentCompteAutred,
                    estLevee: vm.estLevee,
                    page: vm.page2 - 1,
                    size: vm.itemsPerPage
                }, function (result, headers) {
                    console.log('ev', result);
                    vm.evenementsOperation = result;
                    vm.totalItems2 = headers('X-Total-Count');
                    vm.queryCount2 = result.length;
                    console.log('transaction', result);
                    vm.canShowResultCount2 = true;
                })
            }
        }

        function getDetailAudit(personneId) {
            Personne.getAuditOfPersonne({
                id: personneId,
                entite: vm.entite,
                page: vm.page3 - 1,
                size: vm.itemsPerPage
            }, function (result, headers) {
                vm.auditEvent = result;
                console.log('audit--', result);
                vm.totalItems3 = headers('X-Total-Count');
                vm.queryCount3 = result.length;
                vm.canShowResultCount3 = true;
            })
        }
        function getBilanOperation(personneId) {
            console.log('itemSearch', vm.itemSearch);
            Personne.getBilanOperationByPersonne({
                personneId: personneId,
                dateDebut: vm.itemSearch.dateDebut,
                dateFin: vm.itemSearch.dateFin,
                page: vm.page4 - 1,
                size: vm.itemsPerPage
            }, function (result, headers) {
                vm.bilanOp = result;
                console.log('audit--', result);
                vm.totalItems4 = headers('X-Total-Count');
                vm.queryCount4 = result.length;
                vm.canShowResultCount4 = true;
            })
        }

        function getTracePersonne() {
            getUserControl(vm.personneE.id);
        }

        function getUserControl(personneId) {
            console.log('yes start', vm.itemSearch);
            Personne.getUserControle({
                personneId: personneId,
                dateDebut: vm.itemSearch.dateDebut,
                dateFin: vm.itemSearch.dateFin,
                page: vm.page4 - 1,
                size: vm.itemsPerPage
            }, function (result, headers) {
                vm.userControl = result;
                console.log('audit--', result);
                vm.totalItems5 = headers('X-Total-Count');
                vm.queryCount5 = result.length;
                vm.canShowResultCount5 = true;
            })
        }

        function leverEvenement(isActivated, evenementOperation) {
            console.log('oo', isActivated);
            vm.operation = {
                "commentaire": null,
                "id": evenementOperation.id,
                "estLevee": isActivated
            };
            $uibModal.open({
                templateUrl: 'app/entities/operation/account-cash/account-cash-traitement.html',
                controller: 'EvenementTraitementController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return vm.operation;
                    }
                }
            }).result.then(function () {
                Notification.success("Opération effectuée avec succès.");
                $uibModalInstance.close(true);
                $state.reload();
                //$state.go('client',{}, {reload: true});
            });
        }

        function exporterTransClient() {
            vm.itemSearch.type = "CLIENT";
            vm.itemSearch.dateDebut = vm.itemSearch.dateDebutT;
            vm.itemSearch.dateFin = vm.itemSearch.dateFinT;
            console.log('yes lokv', vm.itemSearch);
            if (vm.curentCompteAutred > 0) {
                console.log(vm.curentCompteAutred);
                console.log(vm.itemSearch);
                vm.itemSearch.compteAutreId = vm.curentCompteAutred;
                vm.itemSearch.format = vm.format;
                if (vm.format === null || vm.format === '') {
                    console.log('f', vm.format);
                    vm.itemSearch.format = 'PDF';
                }
                if (vm.itemSearch.format === 'PDF' || vm.itemSearch.format === null)
                {
                    Utils.postPrintUrl(URL, vm.itemSearch);
                } else {
                    if (vm.itemSearch.format === 'XLSX') {
                        console.log('oui');
                        vm.name = 'ExportTransactionClient.xlsx';
                    }
                    if (vm.itemSearch.format === 'CSV') {
                        console.log('oui');
                        vm.name = 'ExportTransactionClient.csv';
                    }
                    Utils.downloadFile(URL, vm.itemSearch, vm.name);
                }
            }

        }

        function ajouterCompteComo(client)
        {
            CancelAlert.confirm(function (arg) {
                console.log('client-----', client);
                Compte.addCompteComo({clientId: client.id}, function (result) {
                    console.log('result--', result);
                    refreshCompte();
                    vm.tail = 1;
                    Notification.success("Opération effectuée avec succès.");
                })
            }, function (arg) {

            })
        }

        vm.setItemsPerPage = function (nbItems) {
            vm.itemsPerPage = nbItems;
            vm.page = 1;
            getDetailTransaction();
        };
        vm.setCurentCompteAutreId = function (compteId) {
            vm.curentCompteAutred = compteId;
            getDetailTransaction();
            getDetailEvenement();
            getBilanOperation(vm.personneE.id);
            getUserControl(vm.personneE.id);

        };
        $scope.$watch("vm.page", function (id) {
            console.log('next');
            getDetailTransaction();
        });
        $scope.$watch("vm.page3", function (id) {
            console.log('next', vm.personneE.id);
            getDetailAudit(vm.personneE.id);
        });
        $scope.$watch("vm.page4", function (id) {
            console.log('next', vm.personneE.id);
            getBilanOperation(vm.personneE.id);
        });
        $scope.$watch("vm.page5", function (id) {
            console.log('next', vm.personneE.id);
            getUserControl(vm.personneE.id);
        });

        function refreshCompte() {
            RequestUtils.ClientInfos.query({
                id: vm.personneE.id
            }, function (datas) {
                vm.client = datas.clientDTO;
                if (vm.client) {
                    vm.oldProfileId = vm.client.profilActeurId;
                }
                vm.comptes = datas.compteDTOS;
                console.log(vm.comptes);
                angular.forEach(vm.comptes, function (value, key) {

                    var compte = value;
                    if (compte.compteAutreId !== null) {
                        RequestUtils.CompteAutreByID.get({id: compte.compteAutreId}, function (donnee) {
                            var compteall = {'cpt': compte, 'compteAutre': donnee};
                            vm.comptesAll.push(compteall);

                        });
                    } else if (compte.compteBanqueId !== null) {
                        vm.comptesBanques.push(compte);
                    } else if (compte.carteId !== null) {
                        vm.comptesCartes.push(compte);
                    }

                });
                if (vm.comptesAll.length > 0) {
                    getDetailTransaction(vm.comptesAll[0].compte);
                }


                vm.documents = datas.documentDTOS;
            });
            vm.disabled = true;
        }

        if (vm.client) {
            vm.personneE = vm.client.personneDTO;
            console.log('entrerrrrrrrrrr------', vm.personneE);
            vm.personnePhysiqueE = vm.client.personnePhysiqueDTO;
            vm.personneMoraleE = vm.client.personneMoraleDTO;
            vm.dataNaissance = null;
            //(vm.client.personnePhysiqueDTO.dateNaissance);
            vm.pieceE = vm.client.pieceDTO;

            if (vm.dataNaissance !== null) {
                console.log(vm.dataNaissance);
                vm.personnePhysiqueE.dateNaissance = vm.dataNaissance;//DateUtils.convertLocalDateFromServer(vm.dataNaissance);
                //   vm.dataNaissance =null;
            }

            RequestUtils.GetPiecesOfTitulaire.query({
                id: vm.personneE.id
            }, function (datas) {
                vm.pieces = datas;
                angular.forEach(vm.pieces, function (value, index) {
                    if (angular.isDefined(value.dateDelivrance))
                        value.dateDelivrance = DateUtils.convertLocalDateFromServer(value.dateDelivrance);

                    if (angular.isDefined(value.dateExpiration))
                        value.dateExpiration = DateUtils.convertLocalDateFromServer(value.dateExpiration);
                });
            });


            RequestUtils.ClientInfos.query({
                id: vm.personneE.id
            }, function (datas) {
                vm.client = datas.clientDTO;

                if (vm.client) {
                    vm.oldProfileId = vm.client.profilActeurId;
                }
                vm.comptes = datas.compteDTOS;
                console.log(vm.comptes);
                angular.forEach(vm.comptes, function (value, key) {

                    var compte = value;
                    if (compte.compteAutreId !== null) {
                        RequestUtils.CompteAutreByID.get({id: compte.compteAutreId}, function (donnee) {
                            var compteall = {'cpt': compte, 'compteAutre': donnee};
                            vm.comptesAll.push(compteall);

                        });
                    } else if (compte.compteBanqueId !== null) {
                        vm.comptesBanques.push(compte);
                        console.log('cpt banue', vm.comptesBanques);

                    } else if (compte.carteId !== null) {
                        vm.comptesCartes.push(compte);
                    }

                });
                if (vm.comptesAll.length > 0) {
                    getDetailTransaction(vm.comptesAll[0].compte);
                }

                console.log('ooo', datas);
                vm.documents = datas.documentDTOS;

                if (vm.personneE.id != null) {
                    getDetailAudit(vm.personneE.id);
                }

            });
            vm.disabled = true;
        } else {
            vm.disabled = false;
        }



        vm.clear = function clear() {
            $scope.modal_instance.close();
            //$uibModalInstance.close();
            //$uibModalInstance.close(vm.popup);
        }

        // console.log(vm.client);
    }
})();
