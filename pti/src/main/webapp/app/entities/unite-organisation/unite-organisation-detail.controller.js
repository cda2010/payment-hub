(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationDetailController', UniteOrganisationDetailController);

    UniteOrganisationDetailController.$inject = ['User','BASE_URL_API','$http', 'CompteBanque', '$uibModal', 'Autorisation', 'AutorisationByUO', 'TypeService', 'Compte', 'CompteByUO', 'ValeurCompte', 'TypeUOWithoutUR', 'Devise', 'EtiquetteCompte', 'TypeCompte', 'Notification', 'Utils','PointVenteByUO', 'PointVente', 'TableService', 'entity', 'RequestUtils', '$timeout', '$scope', '$stateParams', '$uibModalInstance', '$state','CancelAlert','Profil','UniteOrganisationUsers','UniteOrganisation'];

    function UniteOrganisationDetailController(User,BASE_URL_API,$http, CompteBanque, $uibModal, Autorisation, AutorisationByUO, TypeService, Compte, CompteByUO, ValeurCompte, TypeUOWithoutUR, Devise, EtiquetteCompte, TypeCompte, Notification,Utils, PointVenteByUO, PointVente, TableService, entity, RequestUtils, $timeout, $scope, $stateParams, $uibModalInstance, $state,CancelAlert,Profil,UniteOrganisationUsers,UniteOrganisation) {
        var vm = this;
        vm.close = clear;
        vm.savePV = savePV;
        vm.isCompteActor = false;
        vm.isDisabled = false;
        vm.saveCompte = saveCompte;
        vm.saveAutorisation = saveAutorisation;
        vm.uniteOrganisation = entity;
        vm.createPointVente = createPointVente;
        vm.createCompte = createCompte;
        vm.createAutorisation = createAutorisation;
        vm.pointVente = {};
        vm.compte = {};
        vm.autorisation = {};
        vm.query = "";
        vm.typecomptes = TypeCompte.query();
        vm.initCompte = initCompte;
        vm.initPV = initPV;
        vm.initAutorisation = initAutorisation;
        vm.typeservices = [];
        vm.etiquetteComptes = RequestUtils.EtiquetteCompteList.query();
        vm.devises = Devise.query();
        vm.typeuniteorganisations = TypeUOWithoutUR.query();
        vm.typeUOChange = typeUOChange;
        vm.categorieCompteChange = categorieCompteChange;
        vm.codeQr=codeQr;
        vm.ajouterCompteAgent=ajouterCompteAgent;
        vm.tail=0;
        RequestUtils.GetFournisseur.query({}, function (datas) {
            vm.fournisseurservices = datas;
        });
        //Filtrage des utilisateurs
        vm.profils=Profil.query();
        vm.currentAccount = null;
        //vm.setActive = setActive;

        vm.filtrer = false;

        vm.itemSearch = {
            "organisationId": null,
            "profilId": null
        };
        vm.findTransaction=function(compte){
            console.log(compte);
            $uibModal.open({
                templateUrl: 'app/entities/unite-organisation/unite-organisations-transactions.html',
                controller: 'UniteOrganisationTransactionsController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity:  function() {
                        return compte;
                    }
                }
            }).result.then(function() {

            }, function() {
            });
        };
        vm.initFiltre = function () {
            vm.filtrer = !vm.filtrer;
            if (!vm.filtrer) {
                vm.itemSearch = {
                    "organisationId": null,
                    "profilId": null
                };
                vm.organisation=null;
                init();
            }
        };

        vm.organisation = null;
        vm.choisirUo = function () {
            console.log("choisir uo");
            ChoisirUo.open($scope);
        };
        //reinitialisation de mot de passe
        vm.resetPassword=function(user) {
            $uibModal.open({
                templateUrl: 'app/admin/user-management/user-management-detail.html',
                controller: 'UserManagementDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'sm',
                resolve: {
                    entity: ['User', function(User) {
                        return user;
                    }]
                }
            }).result.then(function() {
                vm.loadAll();
                Notification.success("Mot de passe reinitialisé avec succès");
            }, function() {
                Notification.error("L'opération a échoué");
            });
        };
        //Activation/Desactivation de l'utilisateur
        vm.setActive=function setActive(user, isActivated) {
            user.activated = isActivated;
            user.nbrFailure=0;
            User.update(user, function () {
                vm.loadAll();
                vm.clear();
            });
        };

        //recherche des utilisateurs de l'UO
        vm.initUsers=function initUsers() {
            console.log(vm.uniteOrganisation);
            var addItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            login: null,
                            email: null,
                            id: null,
                            uniteOrganisationId:vm.uniteOrganisation.id,
                            fromUO:true,
                            typeUOId:vm.uniteOrganisation.typeUOId
                        };
                    }
                }
            };

            /**
             *Modification d'un élément
             */
            var editItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null
                }
            };

            /**
             * Suppresion d'un élément
             */
            var deleteItemData = {
                templateUrl: 'app/admin/user-management/user-management-delete-dialog.html',
                controller: 'UserManagementDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null
                }
            };

            var detailItemData = {
                templateUrl: 'app/admin/user-management/user-management-dialog.html',
                controller: 'UserManagementDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                deleteData: deleteItemData,
                detailData: detailItemData
            };
            var entitySearchParams = function () {
                return {};
            };

            var entityParams = function () {
                return {
                    "profilId": vm.itemSearch.profilId,
                    "uoId": vm.itemSearch.organisationId,
                    "uniteOrganisationId":vm.uniteOrganisation.id
                };
            };

            var datas = {
                scope: $scope,
                vm: vm,
                entity: UniteOrganisationUsers,
                entitySearch: UniteOrganisationUsers,
                dialogData: dialogData,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            TableService.init(datas);

        };

        function findObjectByKey(array, key, value) {
            for (var i = 0; i < array.length; i++) {
                if (array[i][key] === value) {
                    return array[i];
                }
            }
            return null;
        }

        /*
            Gestion des utilisateurs d'un point de vente
            ajout
        */
        vm.addUsers = function (pointVente) {
            $uibModal.open({
                templateUrl: 'app/entities/point-vente/point-vente-users.html',
                controller: 'PointVenteUsersController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    pointVente: pointVente
                }
            }).result.then(function () {
            }, function () {
            });
        };
        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        $scope.$watch(angular.bind(vm, function () {
            return vm.query;
        }), function (newValue) {
            $scope.filterText = newValue;
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        //POINT DE VENTE

        function savePV() {
            vm.isSaving = true;
            vm.pointVente.uniteOrganisationId = vm.uniteOrganisation.id;
            if (vm.pointVente.id !== null) {
                PointVente.update(vm.pointVente, onSaveSuccessPV, onSaveErrorPV);
            } else {
                PointVente.save(vm.pointVente, onSaveSuccessPV, onSaveErrorPV);
            }
        }

        function initPV() {
            RequestUtils.PointVenteCode.get({ uoId: vm.uniteOrganisation.id }, function (data) {
                vm.pointVente = {};
                vm.pointVente.codePointVente = data.codePointVente;
            }, function () {
            });
        }

        function onSaveSuccessPV(result) {
            $scope.$emit('ptiApp:pointVenteUpdate', result);
            if (vm.pointVente.id == null) {
                Notification.success("Enregistrement réussi");
            } else {
                Notification.success("Modification réussi");
            }
            vm.loadAll();
            initPV();
            vm.isSaving = false;

        }

        function onSaveErrorPV() {
            vm.isSaving = false;
        }

        function createPointVente(event, entity) {
            vm.query = "";
            if (entity == null) {
                initPV();
            } else {
                vm.pointVente = entity;
            }

            var deleteItemDataPV = {
                templateUrl: 'app/entities/point-vente/point-vente-delete-dialog.html',
                controller: 'PointVenteDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogDataPV = {
                deleteData: deleteItemDataPV
            };
            var entitySearchParamsPV = function () {
                return {

                };
            };
            var entityParamsPV = function () {
                return {
                    uoId: vm.uniteOrganisation.id
                };
            };

            var datasPV = {
                scope: $scope,
                vm: vm,
                entity: PointVenteByUO,
                dialogData: dialogDataPV,
                entitySearch: PointVenteByUO,
                entitySearchParams: entitySearchParamsPV,
                entityParams: entityParamsPV
            };
            TableService.init(datasPV);


        }



        //COMPTE

        function typeUOChange() {
            UniteOrganisationByType.query({ typeId: vm.typeUniteOrganisationId }, function (data) {
                vm.uniteorganisations = data;
            }, function () { });
        }

        vm.compteColums = [{
            name: 'etiquette',
            title: 'Etiquette'
        }, {
            name: 'valeurCompte',
            title: 'Numero de Compte'
        }, {
            name: 'uniteOrganisationLibelleUniteOrganisation',
            title: 'Unite Organisation'
        }];

        $scope.$on("onLoadCompte", function () {

        });

        function initCompte() {
            vm.compte = {};
        }

        $scope.$watch("vm.compte.selectCompte", function (boolean) {
            if (boolean && vm.compte.id == null) {
                vm.compte = {};
                vm.compte.selectCompte = true;
            }
        });
        $scope.$watch("vm.compte.etiquette", function (etiquette) {
            if (etiquette != null && vm.compte.selectCompte) {
                RequestUtils.CompteWherePrincipalIsNull.query({ uoId: vm.uniteOrganisation.id, etiquette: vm.compte.etiquette }, function (data) {
                    vm.comptes = data;
                }, function () { });
            }
        });
        
        vm.toggle = function () {
            vm.compte.creerCompte = true;
            vm.isCompteActor = true;
            console.log(vm.compte.creerCompte);
            console.log(vm.isCompteActor);
        };

        $scope.$watch("vm.compte.creerCompte", function (boolean) {
            if (boolean && vm.compte.id == null) {
                vm.compte = {};
                vm.compte.creerCompte = true;
            }
            if (!vm.compte.creerCompte){
                vm.isCompteActor = !vm.isCompteActor;
            }
            if (vm.isCompteActor){
                vm.compte.firstCategorieCompte="COMPTE_EXTERNE";
                vm.compte.etiquette="CPTACTORBANK";
                vm.compte.typeCompteId=2;
                vm.compte.estCompteBanque=true;
            }

        });
        
        


        $scope.$watch("vm.compte.comptePincipaleId", function (id) {
            if (id != null) {
                $http({
                    method: "GET",
                    url: BASE_URL_API + 'compteById',
                    params: { id: id }
                }).then(function mySuccess(response) {
                    vm.comptePincipale = response.data;
                    if (!vm.comptePincipale.isUndefinedOrNull) {
                        vm.compte.valeurCompte = vm.comptePincipale.valeurCompte;
                        vm.compte.typeCompteId = vm.comptePincipale.typeCompteId;
                        vm.compte.firstCategorieCompte = vm.comptePincipale.firstCategorieCompte;
                        vm.compte.categorieCompte = vm.comptePincipale.categorieCompte;
                        vm.compte.etiquette = vm.comptePincipale.etiquette;
                        vm.compte.comptePincipaleId = id;
                    }
                }, function myError(response) {
                });
            }
        });


        function categorieCompteChange() {
            if (vm.compte.firstCategorieCompte == "COMPTE_INTERNE") {
                vm.compte.estCompteBanque = false;
                ValeurCompte.get({}, function (data) {
                    vm.compte.valeurCompte = data.valeurCompte;
                }, function () {
                });
            } else {
                vm.compte.valeurCompte = null;
            }
        }

        function saveCompte() {
            var codeFse = null;
            console.log('vm.compte--', vm.compte.id);
            vm.compte.uniteOrganisationId = vm.uniteOrganisation.id;
            if (vm.compteBanque != null) {
                codeFse = vm.compteBanque.codeFse
            }
            if (vm.compte.creerCompte) {
                vm.compte.comptePincipaleId = null;
                if (vm.compte.estCompteBanque) {
                    vm.compte.categorieCompte = "BANQUE";
                    vm.compte.compteBanqueDTO = vm.compteBanque;
                } else {
                    vm.compte.categorieCompte = "AUTRE";
                    vm.compteAutre = {codeFse: codeFse, solde: 0};
                    vm.compteAutre.solde = 0;
                    vm.compte.compteAutreDTO = vm.compteAutre;
                }
            }
            vm.isSaving = true;
            //  if (vm.compte.id !== null ){
            console.log('update', vm.compte);
            //  if(vm.compte.estCompteBanque){
            //   Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
            //  verifeCompteBanque(vm.compte);
            //  }else {
            //       Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
            // }

            //  } else {
            console.log('create', vm.compte);
            if (vm.compte.estCompteBanque) {
                //      verifeCompteBanque(vm.compte);
                if(vm.compte.id!==null){
                    Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
                }else {
                    Compte.save(vm.compte, onSaveSuccessC, onSaveErrorC);
                }
            } else {
                if (vm.compte.id !== null && vm.compte.id !== undefined) {
                    console.log('update pm', vm.compte);
                    if (vm.compte.estCompteBanque) {
                        Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
                        //verifeCompteBanque(vm.compte);
                    } else {
                        Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
                    }


                } else {
                    console.log('create pm', vm.compte);
                    if (vm.compte.estCompteBanque) {
                        //verifeCompteBanque(vm.compte);
                        Compte.save(vm.compte, onSaveSuccessC, onSaveErrorC);
                    } else {
                        Compte.save(vm.compte, onSaveSuccessC, onSaveErrorC);
                    }

                    //   }
                }
            }
        }

        function verifeCompteBanque(compteBanque) {
            console.log('cpt banque-----',compteBanque);
            RequestUtils.InfoClient.get({
                compte: compteBanque.valeurCompte,
                agence: compteBanque.compteBanqueDTO.codeAgence,
                cle: compteBanque.rib,
                codeFse: compteBanque.compteBanqueDTO.codeFse
            }, function (datas) {
                vm.personneE = datas.personneDTO;
                vm.pieceE = datas.pieceDTO;
                vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                vm.personneMoraleE = datas.personneMoraleDTO;
                console.log('view',datas);
                var message="";
                if(vm.personneE.typePersonne=="PHYSIQUE"){
                    message+="Confirmez les informations du titulaire du compte\n"+ "Nom : "+vm.personnePhysiqueE.nomClient+"\n"+" Prenom : "+vm.personnePhysiqueE.prenomClient + "\n"+"Date naissance : "+vm.personnePhysiqueE.dateNaissance  +"\n "+"Doc Identification : "+vm.pieceE.typePieceLibelleTypePiece+"("+vm.pieceE.referencePiece+")";
                }else{
                    message+="Confirmez les informations du titulaire du compte\n"+ "Nom : "+vm.personnePhysiqueE.nomClient+"\n"+" Prenom : "+vm.personnePhysiqueE.prenomClient + "\n"+"Date naissance : "+vm.personnePhysiqueE.dateNaissance  +"\n "+"Doc Identification : "+vm.pieceE.typePieceLibelleTypePiece+"("+vm.pieceE.referencePiece+")";
                }

                CancelAlert.confirm(function (arg) {
                    console.log(datas);
                    if(vm.compte.id !== null && vm.compte.id!== undefined){
                        Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
                    } else {
                        Compte.save(vm.compte, onSaveSuccessC, onSaveErrorC);
                    }

                }, function (arg) {
                    // body...
                },message, false);


            }, function () {
                Notification.error("COMPTE INTROUVABLE");
            });
        }

        function testCompteBanque(compteBanque) {
            console.log('cpt banque-----',compteBanque);
            vm.personneE= {};
            vm.personnePhysiqueE={};
            vm.pieceE={};
            vm.personneMoraleE={};
            vm.personneE.typePersonne="PHYSIQUE" ;
            vm.personnePhysiqueE.nomClient = 'MAIGA';
            vm.personnePhysiqueE.prenomClient='Abdoul Razack';
            vm.personnePhysiqueE.dateNaissance = '2021-11-12';
            vm.pieceE.typePieceLibelleTypePiece='CNIB';
            vm.pieceE.referencePiece='B47855555';

            //console.log('view',datas);
            var message="";
            if(vm.personneE.typePersonne==="PHYSIQUE"){
                message+="Confirmez les informations du titulaire du compte\n"+ "Nom : "+vm.personnePhysiqueE.nomClient+"\n"+" Prenom : "+vm.personnePhysiqueE.prenomClient + "\n"+"Date naissance : "+vm.personnePhysiqueE.dateNaissance  +"\n "+"Doc Identification : "+vm.pieceE.typePieceLibelleTypePiece+"("+vm.pieceE.referencePiece+")";
            }else{
                message+="Confirmez les informations du titulaire du compte\n"+ "Nom : "+vm.personnePhysiqueE.nomClient+"\n"+" Prenom : "+vm.personnePhysiqueE.prenomClient + "\n"+"Date naissance : "+vm.personnePhysiqueE.dateNaissance  +"\n "+"Doc Identification : "+vm.pieceE.typePieceLibelleTypePiece+"("+vm.pieceE.referencePiece+")";
            }


            CancelAlert.confirm(function (arg) {
                //console.log(datas);
                if(vm.compte.id !== null && vm.compte.id!== undefined){
                    Compte.update(vm.compte, onSaveSuccessC, onSaveErrorC);
                } else {
                    Compte.save(vm.compte, onSaveSuccessC, onSaveErrorC);
                }

            }, function (arg) {
                // body...
            },message, false);

        }


        vm.setActiveCompteBanque=function (compte) {
            console.log('all',compte);
            if (compte.statutCompte==='DESACTIVE' || compte.statutCompte===null) {
                Compte.changeStatutCompteBanque({id:compte.id},function (result) {
                    angular.forEach(vm.datas,function (value,key) {
                        if(value.id===result.id){
                            value.statutCompte=result.statutCompte;
                            console.log('good')
                        }
                    });
                    Notification.success("Statut changé avec succès");
                    console.log(result);
                },function (error) {
                    Notification.error("L'opération a échoué");
                    console.log(error);
                });
            } else {
                console.log('');
                $uibModal.open({
                    templateUrl: 'app/entities/personne/change-status-confirm-dialog.html',
                    controller: 'ChangeStatutConfirmController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        // On injecte 'entity' pour que le contrôleur de la modale puisse le recevoir
                        entity: function () {
                            return compte; 
                        }
                    }
                }).result.then(function() {
                    Compte.changeStatutCompteBanque({id:compte.id},function (result) {
                        angular.forEach(vm.datas,function (value,key) {
                            if(value.id===result.id){
                                value.statutCompte=result.statutCompte;
                                console.log('ok---1')
                            }
                        });
                        Notification.success("Statut changé avec succès");
                        console.log(result);
                    },function (error) {
                        Notification.error("L'opération a échoué");
                        console.log(error);
                    })
                }, function() {
                });
            }
        };


        function onSaveSuccessC(result) {
            $scope.$emit('ptiApp:compteUpdate', result);
            if (vm.compte.id == null) {
                Notification.success("Enregistrement réussi");
            } else {
                Notification.success("Modification réussie");
            }
            vm.compte = {};
            vm.compteAutre = {};
            vm.compteBanque = {};
            vm.loadAll();
            vm.isSaving = false;
        }

        function onSaveErrorC() {
            vm.isSaving = false;
        }

        function ajouterCompteAgent(organisation) {
            console.log('------',organisation);
            CancelAlert.confirm(function (arg) {
                console.log('yes--');
                UniteOrganisation.addCompteOrganisation({organisationId:organisation.id},function (result) {
                    console.log('result--',result);
                   // refreshCompte();
                    vm.tail=1;
                    Notification.success("Enregistrement réussi");
                })
            }, function (arg) {
                console.log('no--');
            })
        }


        function createCompte(event, entity) {
            console.log("mise à jour des comptes");
            vm.query = "";
            if (entity == null) {
                vm.compte = {}
            } else {
                vm.compte = entity;
            }

            var deleteItemData = {
                templateUrl: 'app/entities/compte/compte-delete-dialog.html',
                controller: 'CompteDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {
                };
            };
            var entityParams = function () {
                return {
                    uoId: vm.uniteOrganisation.id
                };
            };

            if (vm.compte.categorieCompte == "BANQUE") {
                vm.compte.estCompteBanque = true;
                CompteBanque.get({ id: vm.compte.compteBanqueId }, function (data) {
                    vm.compteBanque = data;
                }, function () { });
            }

            var datas = {
                scope: $scope,
                vm: vm,
                entity: CompteByUO,
                dialogData: dialogData,
                entitySearch: CompteByUO,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams
            };
            console.log('comptes',datas);
            TableService.init(datas);
        }


        //AUTORISATION

        function saveAutorisation() {
            vm.autorisation.uniteOrganisationId = vm.uniteOrganisation.id;
            vm.isSaving = true;
            if (vm.autorisation.id !== null) {
                Autorisation.update(vm.autorisation, onSaveSuccessA, onSaveErrorA);
            } else {
                Autorisation.save(vm.autorisation, onSaveSuccessA, onSaveErrorA);
            }
        }

        function initAutorisation() {
            vm.query = "";
            vm.typeservices = [];
            vm.autorisation = {};
            vm.autorisation.typeServiceId = null;
            AutorisationByUO.query({ uoId: vm.uniteOrganisation.id }, function (data) {
                TypeService.queryAll({}, function (datas) {
                    angular.forEach(datas, function (value, key) {
                        vm.autorisation = findObjectByKey(data, "typeServiceId", value.id);
                        if (vm.autorisation != null) {
                            value.isSave = true;
                            vm.typeservices.push(value);
                        } else {
                            value.isSave = false;
                            vm.typeservices.push(value);
                        }
                    });
                });
            }, function () { });
        }

        function onSaveSuccessA(result) {
            $scope.$emit('ptiApp:autorisationUpdate', result);
            if (vm.autorisation.id == null) {
                Notification.success("Enregistrement réussi");
            } else {
                Notification.success("Modification réussie");
            }
            vm.isSaving = false;
        }

        function onSaveErrorA() {
            vm.isSaving = false;
        }
        // code qr
        function codeQr(id) {
            vm.item = {
                idChargement:null
            };
            vm.item.idChargement=id;
            console.log(vm.item);
            Utils.postPrintUrl('api/etat/qrCode/UO', vm.item);
        }

        function createAutorisation(event, entity) {
            vm.autorisation = {};
            vm.autorisation.typeServiceId = null;
            vm.typeService = entity;
            if (vm.typeService.isSave == true) {
                vm.autorisation.typeServiceId = vm.typeService.id;
                vm.saveAutorisation();
            } else {
                AutorisationByUO.query({ uoId: vm.uniteOrganisation.id }, function (data) {
                    vm.autorisation = findObjectByKey(data, "typeServiceId", vm.typeService.id);
                    if (vm.autorisation != null) {
                        Autorisation.delete({ id: vm.autorisation.id },
                            function () {
                                Notification.success("Suppression effectuée");
                            });
                    }
                }, function () { });
            }
        }
    }
})();
