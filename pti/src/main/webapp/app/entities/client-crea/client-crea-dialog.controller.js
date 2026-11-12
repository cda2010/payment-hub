(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientCreaDialogController', ClientCreaDialogController);

ClientCreaDialogController.$inject = ['ProfilActeur','GroupeProfilActeur','CancelAlert', 'TypeDocument', 'ServiceProfil', '$scope', 'DataUtils', 'DateUtils', '$stateParams', '$uibModal', 'Notification', 'Nationalite', 'RequestUtils', 'TypePiece', 'InitSteps', '$state', 'Client', 'ClientByTypePersonne','ParametreApp','UniteOrganisation','InfoSupp'];

    function ClientCreaDialogController(ProfilActeur,GroupeProfilActeur,CancelAlert, TypeDocument, ServiceProfil, $scope, DataUtils, DateUtils, $stateParams, $uibModal, Notification, Nationalite, RequestUtils, TypePiece, InitSteps, $state, Client,ClientByTypePersonne,ParametreApp,UniteOrganisation,InfoSupp) {
        var vm = this;
        vm.datas = $stateParams.datas;
            //console.log($state);
        vm.loadPage = loadPage;
        vm.isViewing = $stateParams.isViewing;
        vm.comptes = [];
        vm.documents = [];
        vm.checkcreate=checkcreate;
        //vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;
        vm.client = null;
        vm.datePickerOpenStatus = {};
        vm.createCompte=true;
        vm.clientSaved=false;
        vm.typeDesabled=false;
        var datasToSave = {};
        
        /* Par defaut le compte sera crée**/
        $("#createcompte").prop("checked", true);

       
        vm.typeservices = RequestUtils.ServicesClient.query();
        vm.openCalendar = openCalendar;
        vm.isSaving = false;
        vm.isModeCreate = true;
        vm.indexActuel = null;
        vm.comptes = [];
        vm.localite= {
            id:null,
            nomRegion:null,
            nomProvince: null,
            nomCommune:null,
            nomVille: null
        };
        vm.personnePhysiqueB = {
            photo: null,
            photoContentType: null
        };
        if($state.$current.data.typeP){
            vm.personneB = {
                typePersonne: $state.$current.data.typeP
            };
            vm.typeDesabled=true;
        }else {
            vm.personneB = {
                typePersonne: 'PHYSIQUE'
            };
        }

        vm.estCarte = false;
        var datas = {
            vm: vm,
            steps: vm.steps
        };

        vm.transition = transition;
        vm.typepieces = TypePiece.query();
        vm.nationalites = Nationalite.query();
        vm.ctrlNational = Nationalite.query();
        console.log('nat',vm.ctrlNational);
        vm.profils = ServiceProfil.query();
        vm.groupeProfils=GroupeProfilActeur.queryByType({acteur:'CLIENT'});
        //vm.profilActeurs=ProfilActeur.queryProfileClient({acteur:'CLIENT'});
        vm.typedocuments = TypeDocument.query();
        vm.infosupp=InfoSupp.query();

        RequestUtils.GetOrganisationByType.get({
            type: "PAYS"
        }, function (datas) {
            vm.pays = datas;
        });

        vm.openFile=function(document){
            console.log('doc img',document);
            $uibModal.open({
                templateUrl: 'app/entities/personne/view-file.html',
                controller: 'ViewFileController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    entity: function () {
                        return{
                            documentId:document.id,
                            imageFolder:document.imageFolder,
                            type:document.valeurContentType,
                            valeur: document.valeur
                        }
                    }
                }
            }).result.then(function() {
            }, function() {
            });
        };



        vm.detailsPersonne = function(datas){
            console.log('details des personnes .....');
            console.log(datas);
            $uibModal.open({
                templateUrl: 'app/entities/personne/personne-detail.html',
                controller: 'PersonneDetailController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    entity : function(){
                        return datas;
   }
                }
            }).result.then(function() {
            }, function() {
            }); 
        }
function checkcreate(){
  
    if (  vm.createCompte===true){
  
        
    }else{
      
        
    }
}


        if (vm.datas != null) {
            //var tel = parseInt(vm.datas.personneDTO.telephone);
           // vm.datas.personneDTO.telephone = tel;
            vm.personneB = vm.datas.personneDTO;
            vm.comptes = vm.datas.compteDTOS;
            if (vm.datas.personnePhysiqueDTO != null) {
                vm.personnePhysiqueB = vm.datas.personnePhysiqueDTO;
                vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
            }
            if (vm.datas.personnePhysiqueDTO != null) {
                vm.personneMoraleB = vm.datas.personneMoraleDTO;
            }
            if (vm.datas.pieceDTO != null) {
                vm.pieceB = vm.datas.pieceDTO;
                vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
            }
            vm.client = vm.datas.clientDTO;
            vm.documents = vm.datas.documentDTOS;
        }
        vm.isEmpty = function () {
            return vm.comptes.length === 0;
        };
        vm.steps = [
            'Informations',
            'Documents',
            'Comptes'
        ];

        InitSteps.initSteps(datas);

        function loadPage(page) {
            vm.page = page;
            vm.transition();
        }

        vm.verifDate = function (date) {
            if (!vm.isViewing) {
                var today = new Date();
                if (today > date) {
                    if (angular.isDefined(vm.pieceE) && angular.isDefined(vm.pieceE.dateExpiration))
                        vm.pieceE.dateExpiration = null;

                    if (angular.isDefined(vm.pieceB) && angular.isDefined(vm.pieceB.dateExpiration))
                        vm.pieceB.dateExpiration = null;
                }
                return today > date;
            }
        };

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

        vm.save = function () {
            console.log(vm.localite);
            //vm.personneB.telephone=vm.personneB.telephone.toString();
            console.log('oui ',vm.personneB.telephone);
            vm.isSaving=true;
            if(!vm.clientSaved) {
                //Ajout automatique du compte
                var compte = {
                    codeFse: vm.fseCompteId,
                    categorieCompte: "AUTRE",
                    etiquette:'CPTECLIENT',
                    classeCompte:'PRINCIPAL',
                    etatCompte:'OUVERT',
                    statutCompte:'ACTIVE',
                };
                vm.comptes=[];
                vm.comptes.push(compte);
                console.log(vm.comptes);
                if(vm.personneB.typePersonne=='PHYSIQUE'){
                    vm.personnePhysiqueB.nomClient=vm.personnePhysiqueB.nomClient.toUpperCase();
                    vm.personnePhysiqueB.prenomClient=vm.personnePhysiqueB.prenomClient.toUpperCase();
                }
                datasToSave.personneDTO = vm.personneB;
                datasToSave.personnePhysiqueDTO = vm.personnePhysiqueB;
                datasToSave.personneMoraleDTO = vm.personneMoraleB;
                datasToSave.pieceDTO = vm.pieceB;
                datasToSave.compteDTOS = vm.comptes;
                datasToSave.clientDTO = vm.client;
                datasToSave.codeFse= vm.fseCompteId;
                datasToSave.isCreateCompte=vm.createCompte;
                datasToSave.documentDTOS = vm.documents;
                console.log("document",vm.documents);
                var typeServiceIds = [];
                console.log('piece1', vm.personneB);
                angular.forEach(vm.personneB.typeservices, function (value, index) {
                    if (angular.isDefined(value.isSave) && value.isSave) {
                        typeServiceIds.push(value.id);
                    }
                });
                datasToSave.typeServiceIds = typeServiceIds;
                datasToSave.localite=vm.localite;
                console.log('localite',datasToSave.localite);
                Client.save(datasToSave, function () {
                    if (vm.personneB.typePersonne=='PHYSIQUE'){
                        $state.go("client");
                    }else {
                        $state.go("entreprises");
                    }
                    //vm.clientSaved=true;
                    //vm.incrementStep();
                    if(vm.personneB.typePersonne=='PHYSIQUE') {
                        Notification.success("Client enregistré avec succes!");
                    } else {
                        Notification.success("Entreprise enregistrée avec succes!");
                    }

                }, function () {
                    vm.personneB.telephone= vm.personneB.telephone;
                    vm.isSaving=true;
                });
            }else{
                console.log('piece2', vm.pieceB);
                datasToSave.personneDTO = vm.personneB;
                datasToSave.personnePhysiqueDTO = vm.personnePhysiqueB;
                datasToSave.personneMoraleDTO = vm.personneMoraleB;
                datasToSave.pieceDTO = vm.pieceB;
                datasToSave.compteDTOS = vm.comptes;
                datasToSave.clientDTO = vm.client;
                datasToSave.codeFse= vm.fseCompteId, datasToSave.isCreateCompte=vm.createCompte;
                datasToSave.documentDTOS = vm.documents;
                datasToSave.localite=vm.localite;
                var typeServiceIds = [];
                angular.forEach(vm.personneB.typeservices, function (value, index) {
                    if (angular.isDefined(value.isSave) && value.isSave) {
                        typeServiceIds.push(value.id);
                    }
                })
                datasToSave.typeServiceIds = typeServiceIds;
                //vm.detailsPersonne(datasToSave);
                console.log('localite',datasToSave.localite);
                Client.save(datasToSave, function (result) {
                    console.log(result);
                    $state.go("client");
                    //vm.clientSaved=true;
                    //Notification.success("Souscription terminée!");
                }, function () {

                });
            }
        }
        vm.isModeCreateDoc = true;
        vm.addDocument = function () {
            console.log("Document en cours ....",vm.isModeCreateDoc);
            console.log('-----',vm.documents);
            if (vm.isModeCreateDoc) {
                console.log('created');
                if (vm.document && vm.document.typeDocumentId != null) {
                    var type = _.find(vm.typedocuments, {
                        'id': vm.document.typeDocumentId
                    });
                    if (type) {
                        vm.document.typeDocumentLibelle = type.libelle;
                        vm.document.typeDocumentCode = type.code;
                        vm.documents.push(vm.document);
                        vm.document = null;
                    }
                }

            } else {
                console.log('updated',vm.document);
                if (vm.document && vm.document.typeDocumentId != null) {
                    var type = _.find(vm.typedocuments, {
                        'id': vm.document.typeDocumentId
                    });
                    if (type) {
                        vm.document.typeDocumentLibelle = type.libelle;
                        vm.document.typeDocumentCode = type.code;
                        vm.documents[vm.indexActuelDoc] = vm.document;
                        vm.document = null;
                    }
                }
            }
            vm.isModeCreateDoc = true;

        };


        vm.editDocument = function (index, document) {
            vm.document = document;
            vm.isModeCreateDoc = false;
            vm.indexActuelDoc = index;
        };


        vm.removeDocument = function (index) {
            console.log("Delete document");
            vm.documents.splice(index, 1);
        };

        vm.addCompte = function () {
           // vm.personneB.telephone=vm.personneB.telephone.toString();
            var compte = {
                valeurCompte: vm.personneB.telephone,
                codeFse: vm.fseCompteId,
                categorieCompte: "AUTRE",
                etiquette:'CPTECLIENT',
                classeCompte:'SOUSCOMPTE'
            };
            vm.comptes.push(compte);
            reset();

        };

        vm.editCompte = function (index, compte) {
            if (compte.categorieCompte == "BANQUE") {
                vm.compteBanqueB = compte.compteBanqueDTO;
                vm.compteB = compte.valeurCompte;
                vm.estCompteBanqueB = "OUI";
            } else {
                vm.refCompteB = compte.valeurCompte;
                vm.estCompteBanqueB = "NON";
                vm.fseCompteId = compte.compteAutreDTO.codeFse;
                if (compte.categorieCompte == "CARTE") {
                    vm.estCarte = true;
                }
            }
            vm.isModeCreate = false;
            vm.indexActuel = index;
        };

        vm.reset = reset;

        function reset() {
            vm.compteB = null;
            vm.compteBanqueB = {};
            vm.refCompteB = null;
            vm.estCompteBanqueB = null;
            vm.isModeCreate = true;
            vm.estCarte = false;
        }

        vm.removeCompte = function (index) {
            vm.comptes.splice(index, 1);
        };
        vm.getFournisseurMonnaie=function(idpays) {
            let i=0;
            var pays={};
            for (i=0;i<vm.pays.length;i++){
                if(vm.pays[i].id==idpays){
                    pays=vm.pays[i];
                    break;
                }
            }
            console.log("**************ParamAPP***************");
            console.log(pays.codePays);
            ParametreApp.queryNomAndCode({nom:'FOURNISSEUR_MONNAIE',codeFSE:pays.codePays},function (response) {
                console.log(response);
                if(response.valeur){
                    console.log("**************FSE***************");
                    UniteOrganisation.queryBycodeFSE({codeFSE:response.valeur},function (result) {
                        console.log(result);
                        vm.fseCompteId=result.id;
                    })
                }else {
                    console.log("Aucun fournisseur de monnaie n'a été trouvé");
                }
            });

        };

        vm.getUoFils = function getUoFils(id, type) {
            console.log("recherche infos");
            if (id != "" && type != "" && id != null) {
                RequestUtils.GetUoFilsByType.get({
                    type: type,
                    uoId: id
                }, function (datas) {
                    var filiale = _.find(datas, {
                        'estFiliale': true
                    });
                    vm.fses = datas;
                });
            }
        };

        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.searchPersonne = function () {
            console.log("details des taxes3");
            $uibModal.open({
                templateUrl: 'app/entities/personne/personne-dialog.html',
                controller: 'PersonneDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {}
            }).result.then(function (datas) {
                if (datas != null) {
                    vm.personneB = {};
                    vm.personneMoraleB = {};
                    vm.personnePhysiqueB = {};
                    vm.pieceB = {};
                    console.log("Beneficiaire");
                   // var tel = parseInt(datas.personneDTO.telephone);
                    //datas.personneDTO.telephone = tel;
                    vm.personneB = datas.personneDTO;
                    vm.pieceB = datas.pieceDTO;
                    if (datas.personnePhysiqueDTO != null) {
                        vm.personnePhysiqueB = datas.personnePhysiqueDTO;
                        vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                    }
                    if(datas.personneMoraleDTO != null) {
                        vm.personneMoraleB = datas.personneMoraleDTO;
                        vm.personneMoraleB.dateRCCM = DateUtils.convertLocalDateFromServer(vm.personneMoraleB.dateRCCM);
                    }

                    if (datas.personnePhysiqueDTO != null) {
                        vm.personneMoraleB = datas.personneMoraleDTO;
                    }

                    if (datas.pieceDTO != null) {
                        vm.pieceB = datas.pieceDTO;
                        vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                        vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                    }
                }
            }, function () {

            });;
        };

        vm.setAutotification = function setAutotification($file, personne) {
            console.log($file);
            if ($file && $file.$error === 'pattern') {
                return;
            }
            if ($file) {
                DataUtils.toBase64($file, function (base64Data) {
                    $scope.$apply(function () {
                        //console.log("chargement d'une image "+type);
                        personne.valeur = base64Data;
                        personne.valeurContentType = $file.type;

                    });
                });
            }
        };


        vm.getInfosClientBanque = function (numero, codeAgence, rib, fseId) {
            RequestUtils.InfoClient.get({
                compte: numero,
                agence: codeAgence,
                cle: rib,
                codeFse: fseId
            }, function (datas) {
                Notification.success("COMPTE VALIDE");
            }, function () {
                Notification.console.error();
                ("COMPTE INTROUVABLE");
            });
        }
    }
})();
