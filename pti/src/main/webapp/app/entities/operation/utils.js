(function () {
    "use strict";

    angular.module("ptiApp").factory('OperationUtils', ['MotifOperation', '$uibModal', 'DateUtils', 'TableService', 'RequestUtils', 'Nationalite', 'TypePiece', 'Operation', 'CancelAlert', '$state', 'Notification', function OperationUtils(MotifOperation, $uibModal, DateUtils, TableService, RequestUtils, Nationalite, TypePiece, Operation, CancelAlert, $state, Notification) {

        return {
            "init": init,
            "initVerificationOrganisatioType": initVerificationOrganisatioType
        };

        /**
         * Decodage des informations d'une carte grise
         */
        function init(scope, vm, typeService, uibModalInstance) {
            var vm = vm;
            vm.datePickerOpenStatus = {};
            vm.openCalendar = openCalendar;
            vm.nationalites = Nationalite.query();
            vm.typepieces = TypePiece.query();
            vm.verif = false;
            vm.pieceB = null;
            vm.isFraisDispo = false;
            vm.search = true;
            vm.searchB = true;
            vm.uoFseB = null;
            vm.uoFseE = null;
            vm.compteDTOBeneficiaire=null;
            vm.compteDTOEmetteur=null;
            vm.getUoFils = getUoFils;
            vm.motifs = MotifOperation.query();
            vm.getListEvenement = getListEvenement;

            vm.verifDate = function (date) {
                var today = new Date();
                if (today > date) {
                    if (angular.isDefined(vm.pieceE) && angular.isDefined(vm.pieceE.dateExpiration))
                        vm.pieceE.dateExpiration = null;

                    if (angular.isDefined(vm.pieceB) && angular.isDefined(vm.pieceB.dateExpiration))
                        vm.pieceB.dateExpiration = null;
                }
                return today > date;
            };

            function getUoFils(id, type, typeB) {
                if (id !== "" && type !== "" && id != null && type != null) {
                    RequestUtils.GetUoFilsByType.get({
                        type: type,
                        uoId: id
                    }, function (datas) {
                        var filiale = _.find(datas, {
                            'estFiliale': true
                        });
                        switch (type) {
                            case "FSE":
                                if (typeB === "B") {
                                    vm.organisationFSEB = datas;
                                    vm.uoFseB = filiale.id;
                                } else {
                                    vm.organisationFSE = datas;
                                    vm.uoFseE = filiale.id;
                                }
                                break;
                        }
                        console.log(vm.organisationFSE);
                    });
                }
            }

            //Liste des fse
            vm.getFse = function (uoId, typeB) {
                console.log("recherche du fse du pays");
                vm.getUoFils(uoId, 'FSE', typeB);
                vm.getDefaultDevise(uoId, typeB);
            };


            //Recherche de la devise par defaut d'une organisation
            vm.getDefaultDevise = function (orgId, typeB) {
                RequestUtils.getDefaultDeviseOrg.get({
                    organisationId: orgId
                }, function (datas) {
                    if (typeB == "B") {
                        vm.deviseP = datas;
                    } else {
                        vm.defaultDevise = datas;
                    }
                    vm.getTauxChange(vm.defaultDevise.deviseId, vm.deviseP.deviseId, "ACHAT");
                });
            };

            //Recuperation du taux de change en fonction de la devise source et de la devise dest
            vm.getTauxChange = function (departId, arriveeId, type) {
                RequestUtils.getTauxChange.get({
                    departId: departId,
                    arriveeId: arriveeId,
                    typeChange: type
                }, function (data) {
                    vm.tauxChange = data;
                    console.log('taxe',vm.tauxChange);
                    vm.operation.montantOpEnDevise = vm.tauxChange.taux * vm.operation.montantOp;
                    
                    console.log(vm.operation.montantOpEnDevise );
                });
            };
            //Recuperation de la liste de devises d'un pays
            vm.getAllDevise = function (value) {
                console.log('Liste all devise .....');
                RequestUtils.getDevises.get({
                    organisationId: value
                }, function (datas) {
                    vm.devises = datas;
                    var devise = _.find(vm.devises, {
                        'defaultDevise': true
                    });

                    if (angular.isDefined(devise)) {
                        vm.operation.devisePaiementId = devise.deviseId;
                    }

                });
            };

            //Recuperation de la devise par defaut du pays de l'organisation de l'utilisateur connecté
            RequestUtils.getDefaultDeviseSrc.get({}, function (datas) {
                vm.defaultDevise = datas;
                console.log("default..");
                console.log(vm.defaultDevise);
            });

            vm.traitementOperation = function () {
                console.log('elle');
                $uibModal.open({
                    templateUrl: 'app/entities/operation/account-cash/account-cash-traitement.html',
                    controller: 'OperationTraitementController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        entity: function () {
                            return vm.operation;
                        }
                    }
                }).result.then(function () {
                    Notification.success("Validation effectué avec succès.");
                    uibModalInstance.close(true);
                    //   $state.reload();
                });
            };


            if (vm.operation.id != null) { //_ALL
                Operation.getOneOptimise({
                    id: vm.operation.id
                }, function (datas) {
                    console.log('ok',datas);
                    vm.typeBeneficiaire = datas.typeBeneficiaire;
                    if(datas.beneficiaire!=null){
                        vm.personneB = datas.beneficiaire.personneDTO;
                        vm.personnePhysiqueB = datas.beneficiaire.personnePhysiqueDTO;
                        vm.personneMoraleB = datas.beneficiaire.personneMoraleDTO;
                        vm.pieceB = datas.beneficiaire.pieceDTO;
                        vm.clientB = datas.beneficiaire.clientDTO;
                        vm.comptesB = datas.beneficiaire.compteCBIDTO;
                        vm.compteBanqueB = datas.beneficiaire.compteCBIDTO;
                        if (datas.beneficiaire.compteCBIDTO != null) {
                            vm.compteB = datas.beneficiaire.compteCBIDTO.numeroCompte;
                            if (datas.beneficiaire.compteCBIDTO.type === "Banque") {
                                vm.estCompteBanque = "OUI";
                                vm.estCompteBanqueB = "OUI";
                                vm.compteB = datas.beneficiaire.compteCBIDTO.numeroCompte;
                                vm.compteB = vm.compteBanqueB.numeroCompte;
                            } else {
                                vm.estCompteBanque = "NON";
                                vm.estCompteBanqueB = "NON";
                                vm.refCompteB = datas.beneficiaire.compteCBIDTO.numeroCompte;
                                // vm.compte.numeroCompte=datas.emetteur.compteCBIDTO.numeroCompte;
                            }
                        }
                    }


                    if (vm.pieceB != null) {
                        vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                        vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                    }

                    if(datas.emetteur!=null) {
                        vm.personneE = datas.emetteur.personneDTO;
                        vm.personnePhysiqueE = datas.emetteur.personnePhysiqueDTO;
                        vm.personneMoraleE = datas.emetteur.personneMoraleDTO;
                        vm.pieceE = datas.emetteur.pieceDTO;
                        vm.client = datas.emetteur.clientDTO;
                        vm.compteBanqueE = datas.emetteur.compteCBIDTO;
                        // vm.comptes = datas.emetteur.compteCBIS;
                        if (datas.emetteur.compteCBIDTO != null) {
                            console.log("Infos emetteur");
                            console.log(datas.emetteur);
                            if (datas.emetteur.compteCBIDTO.type === "Banque") {
                                vm.estCompteBanque = "OUI";
                                vm.compte = datas.emetteur.compteCBIDTO.numeroCompte;
                                vm.compteE = vm.compteBanqueE.numeroCompte;

                            } else {
                                vm.estCompteBanque = "NON";
                                vm.refCompte = datas.emetteur.compteCBIDTO.numeroCompte;
                                //vm.compte.numeroCompte=datas.emetteur.compteCBIDTO.numeroCompte;
                            }
                            //vm.compte=datas.emetteur.compteCBIDTO.numeroCompte;
                        }

                    }
                    if(datas.compteDTOEmetteur!=null){
                        vm.compteDTOEmetteur=datas.compteDTOEmetteur;
                    }
                    if(datas.compteDTOBeneficiaire!=null){
                        vm.compteDTOBeneficiaire=datas.compteDTOBeneficiaire;
                    }



                    if (vm.operation.uoAcquereurId  != null) {
                        vm.organisation = {
                            libelleUniteOrganisation: vm.operation.uoAcquereurLibelleUniteOrganisation
                        }; //recuperation du pays de l'acq
                        vm.operation = datas.operationDTO;
                        vm.fseDest = vm.operation.fseDest;
                        vm.fseSrc = vm.operation.fseSrc;
                    }


                    if (vm.personnePhysiqueE != null)
                        vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);
                    if (vm.personnePhysiqueB != null)
                        vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                    if (vm.pieceE != null) {
                        console.log(vm.pieceE);

                        vm.pieceE.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceE.dateDelivrance);
                        vm.pieceE.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceE.dateExpiration);
                    }
                });
                vm.montantOp = vm.operation.montantOp + vm.operation.montantFrais;
            }

            RequestUtils.GetOrganisationByType.get({
                type: "PAYS"
            }, function (datas) {
                vm.pays = datas;
            });

            /**
             *  Recupère la liste des evenements
             * @param {*} montant 
             */
            function getListEvenement(montant) {
                RequestUtils.ListEvenement.query({
                    montant: montant
                }, function (datas) {
                    console.log("liste evenements");
                    console.log(datas);
                    vm.evenements = datas;
                });
            }


            vm.compteBanqueE = {
                codeAgence: null,
                codeBanque: null
            };

            vm.compteBanqueB = {
                codeAgence: null,
                codeBanque: null
            };

            vm.roundedPercentage = function (myValue, totalValue) {
                var result = (myValue * totalValue) / 100;
                return parseFloat(result);
            }

            vm.annulerCalculFrais = function () {
                vm.isFraisDispo = false;
                vm.operation.montantFrais = null;
                vm.montantOp = null;
                vm.operation.montantTaxe = 0;
                vm.operation.statut = null;
            };
            vm.canCalculFrais = function (uoId) {
                if (angular.isDefined(uoId) && angular.isDefined(vm.operation.montantOp)) {
                    return (uoId != null && vm.operation.montantOp != null);
                }
                return false;
            };

            vm.calculFrais = function (uoId, uoEmId) {
                vm.message = "";
                if (vm.operation.lieuPaiementId != null) {
                    uoId = vm.operation.lieuPaiementId;
                }
                if (angular.isDefined(uoId) && angular.isDefined(vm.operation.montantOp)) {
                    RequestUtils.CalculFrais.get({
                        canalLibelle: "WEB",
                        uoAcq: uoId,
                        typeServiceLibelle: typeService,
                        montant: vm.operation.montantOp,
                        uoEmId: uoEmId
                    }, function (data) {
                        vm.message = data.message;
                        vm.isFraisDispo = true;
                        if (data.action == "NON_BLOQUANT") {
                            CancelAlert.confirm(function () {
                                vm.operation.statut = "ENATTENTE_VALIDATION";
                                doTraitement(data,uoId,typeService);
                            }, function () {
                                vm.operation.montantOp = null;
                                vm.isFraisDispo = false;
                            }, "L'opération en cours sera soumise pour validation!\n Voulez vous continuez?");
                        }else{
                            doTraitement(data,uoId,typeService);
                        }
                    });
                } else {}
            };

            function doTraitement(data,uoId,typeService){
                console.log('Do calcul---');
                vm.montantOp = vm.operation.montantOp;
                vm.isFraisDispo = true;
                if (data != null) {
                    if (data.valeur != null) {
                        vm.operation.montantFrais = data.valeur;
                    } else {
                        vm.operation.montantFrais = 0;
                    }

                    if (data.typeFrais == "FIXE") {
                        vm.montantOp = vm.operation.montantOp + vm.operation.montantFrais;
                    } else if (data.typeFrais == "POURCENTAGE") { //POURCENTAGE
                        vm.operation.montantFrais = vm.roundedPercentage(data.valeur, vm.montantOp);
                        vm.montantOp = vm.operation.montantOp + vm.operation.montantFrais;
                    }
                }
                if (vm.operation.montantFrais > 0) {
                    //Calcul de la tva
                    RequestUtils.getTaxes.get({
                        organisationId: uoId,
                        service: typeService,
                        montantOp: vm.operation.montantOp,
                        montantFrais: vm.operation.montantFrais
                    }, function (data) {
                        angular.forEach(data, function (value, index) {
                            vm.operation.montantTaxe = value.montant + vm.operation.montantTaxe;
                            if (value.appliqueSur == "MONTANT") {
                                // vm.operation.montantFrais=vm.operation.montantFrais+value.montant;
                            } else if (value.appliqueSur == "FRAIS") {
                                vm.operation.montantFrais = vm.operation.montantFrais - value.montant;
                            }
                        });
                        vm.taxes = data;
                    });
                }
                 //Recuperation du taux de change et conversion
                if (vm.typeService != "ENVOI_CASH")
                    vm.getTauxChange(vm.defaultDevise.deviseId, vm.deviseP.deviseId, "ACHAT");
            }


            vm.datePickerOpenStatus.dateOperation = false;

            function openCalendar(date) {
                vm.datePickerOpenStatus[date] = true;
            }

            vm.eventBeneficiaire = function () {
                if (vm.verif) {
                    vm.canEdit(vm.personneB, vm.pieceB, "B", true);
                }
            };
            scope.$watch('vm.pieceB.typePieceId', function () {
                vm.eventBeneficiaire();
            });

            scope.$watch('vm.pieceB.referencePiece', function () {
                vm.eventBeneficiaire();
            });

            scope.$watch('vm.personneB.telephone', function () {
                vm.eventBeneficiaire();
            });

            vm.eventEmetteur = function () {
                if (vm.verif) {
                    vm.canEdit(vm.personneE, vm.pieceE, "E", true);
                }
            };
            scope.$watch('vm.pieceE.typePieceId', function () {
                vm.eventEmetteur();
            });

            scope.$watch('vm.pieceE.referencePiece', function () {
                vm.eventEmetteur();
            });

            scope.$watch('vm.personneE.telephone', function () {
                vm.eventEmetteur();
            });

            vm.initCompte = function (type, numero, comptes) {
                if (vm.operation.id == null) {
                    var cmpt = _.find(comptes, {
                        'numeroCompte': numero
                    });
                    if (type == "E") {
                        vm.compteBanqueE.codeBanque = cmpt.codeBanque;
                        vm.compteBanqueE.codeAgence = cmpt.codeAgence;
                    } else {
                        vm.compteBanqueB.codeBanque = cmpt.codeBanque;
                        vm.compteBanqueB.codeAgence = cmpt.codeAgence;
                    }
                }
            };

            vm.simulateur = function () {
                console.log('simulateur de frais');
                $state.go('simulation-operation');
            };

            /*
            Recherche d'un Client avec le codeClient à travers l'omnicanal
            */
            vm.getInfosClient = function (type, cle, agence, numero, codeFse) {
                RequestUtils.InfoClient.get({
                    compte: numero,
                    cle: cle,
                    agence: agence,
                    codeFse: codeFse
                }, function (datas) {
                    if (type == "E") {
                        console.log("Les inforamations du compte E");
                        vm.search = false;
                        vm.personneE = datas.personneDTO;
                        vm.pieceE = datas.pieceDTO;
                        vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                        vm.personneMoraleE = datas.personneMoraleDTO;
                        vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);
                        vm.pieceE.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceE.dateDelivrance);
                        vm.pieceE.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceE.dateExpiration);
                        vm.comptes = datas.compteCBIDTO;
                    } else {
                        console.log("Les inforamations du compte B");
                        vm.searchB = false;
                        vm.personneB = datas.personneDTO;
                        vm.pieceB = datas.pieceDTO;
                        vm.personnePhysiqueB = datas.personnePhysiqueDTO;
                        vm.personneMoraleB = datas.personneMoraleDTO;
                        vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                        vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                        vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                        vm.comptesB = datas.compteCBIDTO;
                    }
                }, function () {
                    if (type == "E") {
                        vm.pieceE = null;
                        vm.personnePhysiqueE = null;
                        vm.personneMoraleE = null;
                        vm.comptesB = null;
                        vm.compteDTOE = null;
                    } else {
                        vm.pieceB = null;
                        vm.personnePhysiqueB = null;
                        vm.personneMoraleB = null;
                        vm.comptesB = null;
                        vm.compteDTOB = null;
                    }
                });
            };

            vm.getInfosClientSouscrit = function (type, telephone, numero, codeService, codeAgence, rib, fseId, codeTelephone) {
                RequestUtils.InfoClientSouscri.get({
                    codeTelephone: codeTelephone,
                    telephone: telephone,
                    numCompte: numero,
                    codeService: codeService,
                    codeAgence: codeAgence,
                    rib: rib,
                    fseId: fseId
                }, function (datas) {
                    console.log(datas);
                    if (type == "E") {
                        console.log("Les inforamations du compte E");
                        vm.search = false;
                        vm.personneE = datas.personneDTO;
                        vm.pieceE = datas.pieceDTO;
                        vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                        vm.personneMoraleE = datas.personneMoraleDTO;
                        vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);
                        vm.pieceE.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceE.dateDelivrance);
                        vm.pieceE.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceE.dateExpiration);
                        vm.comptes = datas.compteCBIDTO;
                        vm.compteDTOE = datas.compteDTOS[0];
                        console.log(vm.compteDTOE);
                    } else {
                        console.log("Les inforamations du compte B");
                        vm.searchB = false;
                        vm.personneB = datas.personneDTO;
                        vm.pieceB = datas.pieceDTO;
                        vm.personnePhysiqueB = datas.personnePhysiqueDTO;
                        vm.personneMoraleB = datas.personneMoraleDTO;
                        vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                        vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                        vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                        vm.comptesB = datas.compteCBIDTO;
                        vm.compteDTOB = datas.compteDTOS[0];
                        console.log(vm.compteDTOB);

                    }
                }, function () {
                    if (type == "E") {
                        vm.pieceE = null;
                        vm.personnePhysiqueE = null;
                        vm.personneMoraleE = null;
                        vm.comptesB = null;
                        vm.compteDTOE = null;
                    } else {
                        vm.pieceB = null;
                        vm.personnePhysiqueB = null;
                        vm.personneMoraleB = null;
                        vm.comptesB = null;
                        vm.compteDTOB = null;
                    }
                });
            }


            vm.detailTaxes = function () {
                $uibModal.open({
                    templateUrl: 'app/entities/taxe-operation/taxeOperation-detail.html',
                    controller: 'TaxeOperationDetailController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {
                        taxes: function () {
                            return vm.taxes;
                        },
                        total: function () {
                            return vm.operation.montantTaxe;
                        }
                    }
                });
            };

            vm.searchPersonne = function () {
                console.log("details des taxes1");
                $uibModal.open({
                    templateUrl: 'app/entities/personne/personne-dialog.html',
                    controller: 'PersonneDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'md',
                    resolve: {}
                }).result.then(function (datas) {
                    var type = "E";
                    console.log("in operation");
                    console.log(datas);
                    if (datas != null) {
                        if (type == "B") { //Beneficiaire
                            vm.personneB = {};
                            vm.personneMoraleB = {};
                            vm.personnePhysiqueB = {};
                            vm.pieceB = {};
                            console.log("Beneficiaire");
                            vm.personneB = datas.personneDTO;
                            vm.pieceB = datas.pieceDTO;
                            if (datas.personnePhysiqueDTO != null) {
                                vm.personnePhysiqueB = datas.personnePhysiqueDTO;
                                vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                            }
                            if (datas.personnePhysiqueDTO != null) {
                                vm.personneMoraleB = datas.personneMoraleDTO;
                            }

                            if (datas.pieceDTO != null) {
                                vm.pieceB = datas.pieceDTO;
                                vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                                vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                            }
                        } else {
                            console.log("emetteur...");
                            //Emetteur
                            vm.personneE = {};
                            vm.personneMoraleE = {};
                            vm.personnePhysiqueE = {};
                            vm.pieceE = {};
                            vm.personneE = datas.personneDTO;
                            if (datas.pieceDTO != null) {
                                vm.pieceE = datas.pieceDTO;
                                vm.pieceE.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceE.dateDelivrance);
                                vm.pieceE.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceE.dateExpiration);
                            }

                            if (datas.personnePhysiqueDTO) {
                                vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                                vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);
                            }

                            if (datas.personneMoraleDTO) {
                                vm.personneMoraleE = datas.personneMoraleDTO;
                            }
                        }
                    }
                }, function () {

                });
            };
        }

        function initVerificationOrganisatioType(vm) {

            vm.getAllDevise = function (value) {
                RequestUtils.getDevises.get({
                    organisationId: value
                }, function (datas) {
                    vm.devises = datas;
                });
            };
            vm.userOrganisation = null;
            RequestUtils.UserOrganisation.get({}, function (data) {
                vm.userOrganisation = data;
                console.log( data);
                vm.getAllDevise(vm.userOrganisation.id);
            });

            vm.isMaster = function () {
               
                if (vm.userOrganisation != null) {
                    if (vm.userOrganisation.typeUOLibelleTypeUO == "MASTER" || vm.userOrganisation.typeUOLibelleTypeUO == "SOUS_DISTRIBUTEUR") {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            };

            vm.isMasterOnly = function () {
                if (vm.userOrganisation != null) {
                    if (vm.userOrganisation.typeUOLibelleTypeUO == "MASTER") {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            };

            vm.isAgence = function () {
                if (vm.userOrganisation != null) {
                    if (vm.userOrganisation.typeUOLibelleTypeUO == "AGENCE") {
                        return true;
                    } else {
                        return false;
                    }
                }
                return false;
            }
        }


    }]);
})();
