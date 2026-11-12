(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ClientDialogController', ClientDialogController);

    ClientDialogController.$inject = ['ProfilActeur','GroupeProfilActeur','CancelAlert', 'TypeDocument', 'ServiceProfil', '$scope', 'DataUtils', 'DateUtils', '$stateParams', '$uibModal', 'Notification', 'Nationalite', 'RequestUtils', 'TypePiece', 'InitSteps', '$state', 'Client','ParametreApp','UniteOrganisation','InfoSupp'];

    function ClientDialogController(ProfilActeur,GroupeProfilActeur,CancelAlert, TypeDocument, ServiceProfil, $scope, DataUtils, DateUtils, $stateParams, $uibModal, Notification, Nationalite, RequestUtils, TypePiece, InitSteps, $state, Client,ParametreApp,UniteOrganisation,InfoSupp) {
        var vm = this;
        vm.nationalites;
        vm.datas = $stateParams.datas;
        vm.back=$stateParams.back;
        console.log('update',vm.datas);
        vm.loadPage = loadPage;
        vm.findValCpt=findValCpt;
        vm.isViewing = $stateParams.isViewing;
        vm.documents = [];
        vm.num=null;
        vm.pereTypeUO='UNITE_REGROUPEMENT';
        //vm.openFile = DataUtils.openFile;
        vm.byteSize = DataUtils.byteSize;
        vm.client = null;
        vm.datePickerOpenStatus = {};
        vm.typeservices = RequestUtils.ServicesClient.query();
        vm.openCalendar = openCalendar;
        vm.isSaving = false;
        vm.isModeCreate = true;
        vm.testIsCreate=false;
        vm.indexActuel = null;
        vm.comptes = [];
        vm.infosupp=InfoSupp.query();
        vm.compteIdRemoved=[];

        vm.statutCompte =null;
        vm.etatCompte=null;
        vm.personnePhysiqueB = {
            photo: null,
            photoContentType: null
        };
        vm.typeDesabled=false;
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
        vm.profils = ServiceProfil.query();
        vm.groupeProfils=GroupeProfilActeur.queryByType({acteur:'CLIENT'});
        //vm.profilActeurs=ProfilActeur.query();
        vm.typedocuments = TypeDocument.query();
        vm.typeCompteChange=function(){
            if(vm.estCompteBanqueB=='OUI'){
                vm.refCompteB=null;
            }else {
                vm.refCompteB=vm.generateValeurCompte();
            }
        };
        vm.generateValeurCompte=function(){
            let racine=null;
            let suffixe=0;
            if(vm.comptes){
                angular.forEach(vm.comptes, function (compte, index) {
                    if(compte.categorieCompte== "AUTRE"){
                        suffixe ++;
                        if(compte.classeCompte== "PRINCIPAL"){
                            racine=compte.valeurCompte;
                        }
                    }
                })
            }
            if(racine){
                return racine+'_'+suffixe;
            }
            return null;
        };
        vm.findProfils=function(idgrp){
            ProfilActeur.queryByGroupe({groupeId:idgrp},function (result) {
                vm.profilActeurs=result;
            })
        };
        vm.goBack=function(){
            $state.go(vm.back);
        };

        RequestUtils.GetOrganisationByType.get({
            type: "PAYS"
        }, function (datas) {
            vm.pays = datas;
            console.log('turn', vm.pays)
        });
        UniteOrganisation.queryByType({typeId:vm.pereTypeUO},function (result) {
            console.log(result);
            vm.uniteorganisations=result;
        });

        vm.openFile=function(document){
            console.log('ok',document);
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



        if (vm.datas != null) {
            vm.testIsCreate=true;
            console.log('111',vm.datas);
            vm.personneE = vm.datas.personneDTO;
            console.log(vm.personneE);
            RequestUtils.ClientInfos.query({
                id:vm.personneE.id
            },function (result) {
                console.log('imag',result.documentDTOS);
                vm.docNew=result.documentDTOS;
                console.log('imag vr',vm.docNew);
                vm.personneB = vm.datas.personneDTO;
                vm.localite={
                    id: vm.datas.personneDTO.localiteId,
                    nomRegion: vm.datas.personneDTO.localiteRegion,
                    nomProvince: vm.datas.personneDTO.localiteProvince,
                    nomCommune: vm.datas.personneDTO.localiteCommune,
                    nomVille: vm.datas.personneDTO.localiteVille
                };
                console.log('loca',vm.datas);
                console.log('result',result);
                vm.comptes = vm.datas.compteDTOS;
                console.log('///',vm.datas.personneDTO.memo);
                vm.personneB.memo=result.personneDTO.memo;
                if (vm.datas.personnePhysiqueDTO != null) {
                    console.log('vm.datas.personnePhysiqueDTO',vm.datas.personnePhysiqueDTO);
                    console.log('vm.datas.personnePhysiqueDTO.profession',vm.datas.personnePhysiqueDTO.profession);
                    vm.personnePhysiqueB = vm.datas.personnePhysiqueDTO;
                    vm.personnePhysiqueB.nomMereClient=vm.datas.personnePhysiqueDTO.nomMereClient;
                    vm.personnePhysiqueB.profession=result.personnePhysiqueDTO.profession;
                    vm.personnePhysiqueB.personnePrevenir=vm.datas.personnePhysiqueDTO.personnePrevenir;
                    vm.personnePhysiqueB.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueB.dateNaissance);
                }else {
                    vm.personneMoraleB = vm.datas.personneMoraleDTO;
                    vm.personneMoraleB.dateRCCM = DateUtils.convertLocalDateFromServer(vm.personneMoraleB.dateRCCM);
                }

                if (vm.datas.pieceDTO != null) {
                    vm.pieceB = vm.datas.pieceDTO;
                    vm.pieceB.dateDelivrance = DateUtils.convertLocalDateFromServer(vm.pieceB.dateDelivrance);
                    vm.pieceB.dateExpiration = DateUtils.convertLocalDateFromServer(vm.pieceB.dateExpiration);
                    console.log('date del',vm.pieceB.dateDelivrance);
                    console.log('date ex',vm.pieceB.dateExpiration);
                }
                console.log('end doc',vm.datas.personnePhysiqueDTO);
                vm.client = vm.datas.clientDTO;
                vm.client.infosuppId=result.clientDTO.infosuppId;
                vm.documents = vm.docNew;
                console.log('end+++',vm.datas.clientDTO);
                Client.getComptePrincipal({clientId:vm.client.id},function (reponse) {
                    if(reponse.id!== null )
                    {
                        vm.num= reponse.valeurCompte;
                        console.log('tik',vm.num);
                    } else {
                        console.log('sorry');
                    }

                });
            });

        }
        if(vm.datas!=null) {

        }
        vm.isEmpty = function () {
            return vm.comptes.length === 0;
        }
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
            console.log('okklll',vm.pieceE);
            if (!vm.isViewing) {
                var today = new Date();
                if (today > date) {
                    if (angular.isDefined(vm.pieceE) && angular.isDefined(vm.pieceE.dateExpiration))
                        console.log('okklll',vm.pieceE);
                        vm.pieceE.dateExpiration = null;

                    if (angular.isDefined(vm.pieceB) && angular.isDefined(vm.pieceB.dateExpiration))
                        console.log('okklll');
                        vm.pieceB.dateExpiration = null;
                }
                return today > date;
            }
        };
        vm.getFournisseurMonnaieS=function(idpays) {
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
                    console.log("**************FSE***************Souscription");
                    UniteOrganisation.queryBycodeFSE({codeFSE:response.valeur},function (result) {
                        console.log(result);
                        vm.fseCompteId=result.id;
                    })
                }else {
                    console.log("Aucun fournisseur de monnaie n'a été trouvé");
                }
            });

        };

        function transition() {
            $state.transitionTo($state.$current, {
                page: vm.page,
                sort: vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc'),
                search: vm.currentSearch
            });
        }

        vm.save = function () {
            if(vm.datas==null){
                console.log("---------Create compte default---------------");
                var compte = {
                    codeFse: vm.fseCompteId,
                    categorieCompte: "AUTRE",
                    etiquette:'CPTECLIENT',
                    classeCompte:'PRINCIPAL',
                    etatCompte:'OUVERT',
                    statutCompte:'ACTIVE'
                };
                vm.comptes.push(compte);
                console.log("-------Compte-----------",compte);
                console.log("-------Tab-----------",vm.comptes)
            }
            console.log('date ok',vm.pieceB);
            //vm.personneB.telephone=vm.personneB.telephone.toString();
            if(vm.personneB.typePersonne=='PHYSIQUE'){
                vm.personnePhysiqueB.nomClient=vm.personnePhysiqueB.nomClient.toUpperCase();
                vm.personnePhysiqueB.prenomClient=vm.personnePhysiqueB.prenomClient.toUpperCase();
            }
            vm.isSaving=true;
            var datas = {};
            datas.personneDTO = vm.personneB;
            datas.personnePhysiqueDTO = vm.personnePhysiqueB;
            datas.personneMoraleDTO = vm.personneMoraleB;
            datas.pieceDTO = vm.pieceB;
            datas.compteDTOS = vm.comptes;
            datas.clientDTO = vm.client;
            datas.documentDTOS = vm.documents;
            datas.compteIdRemoved=vm.compteIdRemoved;
            console.log('PersPhysique', vm.documents);
            console.log(vm.comptes,'et',vm.personneB);
            var typeServiceIds = [];
            angular.forEach(vm.personneB.typeservices, function (value, index) {
                if (angular.isDefined(value.isSave) && value.isSave) {
                    typeServiceIds.push(value.id);
                }
            });
            datas.typeServiceIds = typeServiceIds;
            datas.localite=vm.localite;
            console.log('data',datas);
            Client.save(datas, function () {
                if (vm.personneB.typePersonne=='PHYSIQUE'){
                    $state.go("client");
                }else {
                    $state.go("entreprises");
                }
                if(vm.testIsCreate){
                    Notification.success("Modification effectuée avec succès !");
                } else {
                    Notification.success("Client enregistré avec succès !");
                }

            }, function () {

            });
        };
        vm.isModeCreateDoc = true;
        vm.addDocument = function () {
            console.log("Document en cours ....",vm.document);
            console.log("Document old....",vm.documents);
            if (vm.isModeCreateDoc) {
                console.log("Created ....");
                if (vm.document && vm.document.typeDocumentId != null) {
                    var type = _.find(vm.typedocuments, {
                        'id': vm.document.typeDocumentId
                    });
                    if (type) {
                        vm.document.typeDocumentLibelle = type.libelle;
                        vm.document.typeDocumentCode =type.code;
                        vm.documents.push(vm.document);
                        vm.document = null;
                    }
                }
            } else {
                console.log("updated ....");
                if (vm.document && vm.document.typeDocumentId != null) {
                    var type = _.find(vm.typedocuments, {
                        'id': vm.document.typeDocumentId
                    });
                    if (type) {
                        vm.document.typeDocumentLibelle = type.libelle;
                        vm.document.typeDocumentCode =type.code;
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
            console.log('yu updated ?',vm.document)
        };


        vm.removeDocument = function (index) {
            console.log("Delete document");
            vm.documents.splice(index, 1);
        };

               vm.addCompte = function () {
            // if((vm.isModeCreate|| vm.estCompteBanqueB != "NON"|| vm.comptes[vm.indexActuel].compteAutreDTO.solde)&&vm.etatCompte!='OUVERT'){
            //     vm.etatCompte='OUVERT';
            //     alert("Vous ne pouvez pas fermer ce compte")
            // }
            vm.cpt = "";
            vm.cle = "";
            vm.codeAgence = "";
             vm.last4Digit = "";
           vm.nomOnCard="";
            console.log(vm.compteB);
            if (vm.estCompteBanqueB == "OUI" || vm.estCompteBanqueB === "CARTE") {
                if (vm.estCompteBanqueB === "CARTE") {
                    vm.cpt = vm.compteB;
                    vm.cle = vm.compteBanqueB.codeAgence;
                    vm.codeAgence = vm.compteBanqueB.codeAgence;
                    vm.last4Digit = vm.compteBanqueB.codeAgence;
                } else {
                    vm.cpt = vm.compteB;
                    vm.cle = vm.compteBanqueB.rib;
                    vm.codeAgence = vm.compteBanqueB.codeAgence;
                }
                RequestUtils.InfoClient.get({
                    compte: vm.compteB,
                    agence: vm.codeAgence,
                    cle: vm.cle,
                    codeFse: vm.fseCompteId
                }, function (datas) {
                    vm.personneE = datas.personneDTO;
                    vm.pieceE = datas.pieceDTO;
                    vm.personnePhysiqueE = datas.personnePhysiqueDTO;
                    vm.categorieCompte = "";
                    vm.personneMoraleE = datas.personneMoraleDTO;
                    console.log('view', datas);
                    var message = "";
                    if (vm.personneE.typePersonne == "PHYSIQUE") {
                         vm.nomOnCard=vm.personnePhysiqueE.nomClient+" "+ vm.personnePhysiqueE.prenomClient  ;
                        message += "Confirmez les informations du titulaire du compte\n" + "Nom : " + vm.personnePhysiqueE.nomClient + "\n" + " Prenom : " + vm.personnePhysiqueE.prenomClient + "\n" + "Date naissance : " + vm.personnePhysiqueE.dateNaissance + "\n " + "Doc Identification : " + vm.pieceE.typePieceLibelleTypePiece + "(" + vm.pieceE.referencePiece + ")";
                    } else {
                         vm.nomOnCard=vm.personneMoraleE.raisonSociale ;
                        message += "Confirmez les informations du titulaire du compte\n" + "Raison Sociale : " + vm.personneMoraleE.raisonSociale + "\n" + " Nom Dirigeant : " + vm.personneMoraleE.dirigeant + "\n" + "Date RCCM : " + vm.personneMoraleE.dateRCCM + "\n " + "Numero IFU : " + vm.personneMoraleE.numreoIfu + "";
                    }
                    //  vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);
                    CancelAlert.confirm(function (arg) {
                        vm.compte
                        console.log(datas);
                        if (vm.estCompteBanqueB === "CARTE") {
                            vm.categorieCompte = "CARTE";

                            vm.compte = {
                                valeurCompte: vm.compteB,
                                codeFse: vm.fseCompteId,

                                carteDTO: {
                                    codeFse: vm.fseCompteId,
                                    last4Digit:  vm.last4Digit,
                                    nomOnCard: vm.nomOnCard
                                },
                                categorieCompte: vm.categorieCompte
                                        // etatCompte:vm.etatCompte,
                                        // statutCompte:vm.statutCompte
                            };
                        } else {
                            vm.categorieCompte = "BANQUE";
                            vm.compte = {
                                valeurCompte: vm.compteB,
                                codeFse: vm.fseCompteId,
                                compteBanqueDTO: vm.compteBanqueB,
                                categorieCompte: vm.categorieCompte
                                        // etatCompte:vm.etatCompte,
                                        // statutCompte:vm.statutCompte
                            };

                        }
                        if (vm.isModeCreate) {
                            vm.comptes.push(vm.compte);
                        } else {
                            vm.comptes[vm.indexActuel].valeurCompte = vm.compteB;
                            vm.comptes[vm.indexActuel].codeFse = vm.fseCompteId;
                            vm.comptes[vm.indexActuel].compteBanqueDTO = vm.compteBanqueB;
                            vm.comptes[vm.indexActuel].categorieCompte = vm.categorieCompte;
                            // vm.comptes[vm.indexActuel].etatCompte = vm.etatCompte;
                            // vm.comptes[vm.indexActuel].statutCompte = vm.statutCompte;
                        }
                        reset();
                    }, function (arg) {
                        // body...
                    }, message, false);
                }, function () {
                    Notification.console.error("COMPTE INTROUVABLE");
                });
            } else {

                if (vm.isModeCreate) {
                    var compte = {
                        valeurCompte: vm.refCompteB,
                        codeFse: vm.fseCompteId,
                        categorieCompte: vm.estCarte == true ? "CARTE" : "AUTRE",
                        compteAutreDTO: {
                            codeFse: vm.fseCompteId
                        },
                        etatCompte: vm.etatCompte,
                        statutCompte: vm.statutCompte
                    };
                    vm.comptes.push(compte);
                } else {
                    vm.comptes[vm.indexActuel].valeurCompte = vm.refCompteB;
                    vm.comptes[vm.indexActuel].codeFse = vm.fseCompteId;
                    vm.comptes[vm.indexActuel].compteAutreDTO = {
                        codeFse: vm.fseCompteId
                    };
                    vm.comptes[vm.indexActuel].categorieCompte = vm.estCarte == true ? "CARTE" : "AUTRE";
                    vm.comptes[vm.indexActuel].etatCompte = vm.etatCompte;
                    vm.comptes[vm.indexActuel].statutCompte = vm.statutCompte;
                }
                reset();
            }
            console.log(vm.comptes)
        };

        vm.addCompteBanque = function () {
            console.log(vm.compteB);
            if (vm.estCompteBanqueB == "OUI") {
                vm.personneE = vm.datas.personneDTO;
                vm.pieceE = vm.datas.pieceDTO;
                vm.personnePhysiqueE = vm.datas.personnePhysiqueDTO;
                vm.personneMoraleE = vm.datas.personneMoraleDTO;
                console.log('view', vm.datas);
                var message = "";
                if (vm.personneE.typePersonne == "PHYSIQUE") {
                    message += "Confirmez les informations du titulaire du compte\n" + "Nom : " + vm.personnePhysiqueE.nomClient + "\n" + " Prenom : " + vm.personnePhysiqueE.prenomClient + "\n" + "Date naissance : " + vm.personnePhysiqueE.dateNaissance + "\n " + "Doc Identification : " + vm.pieceE.typePieceLibelleTypePiece + "(" + vm.pieceE.referencePiece + ")";
                } else {
                    message += "Confirmez les informations du titulaire du compte\n" + "Nom : " + vm.personnePhysiqueE.nomClient + "\n" + " Prenom : " + vm.personnePhysiqueE.prenomClient + "\n" + "Date naissance : " + vm.personnePhysiqueE.dateNaissance + "\n " + "Doc Identification : " + vm.pieceE.typePieceLibelleTypePiece + "(" + vm.pieceE.referencePiece + ")";
                }
                //  vm.personnePhysiqueE.dateNaissance = DateUtils.convertLocalDateFromServer(vm.personnePhysiqueE.dateNaissance);

                CancelAlert.confirm(function (arg) {
                    console.log(datas);
                    if (vm.isModeCreate) {
                        var compte = {
                            valeurCompte: vm.compteB,
                            codeFse: vm.fseCompteId,
                            compteBanqueDTO: vm.compteBanqueB,
                            categorieCompte: "BANQUE",
                            // etatCompte:vm.etatCompte,
                            // statutCompte:vm.statutCompte
                        };
                        vm.comptes.push(compte);
                    }
                })
            }

        };


        vm.editCompte = function (index, compte) {
            console.log(compte);
            vm.statutCompte=compte.statutCompte;
            vm.etatCompte=compte.etatCompte;
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
            vm.refCompteB = vm.generateValeurCompte();
            vm.estCompteBanqueB = null;
            vm.isModeCreate = true;
            vm.estCarte = false;
            console.log(vm.refCompteB)
        }

        vm.removeCompte = function (index,compte) {
            console.log(compte,index);
            CancelAlert.confirm(function (datas) {
                console.log('ok',compte);
                vm.compteIdRemoved.push(compte.compteBanqueDTO.id);
                vm.comptes.splice(index, 1);
                console.log('rm',vm.compteIdRemoved);
                Client.deleteCompteBanqueOfClient({compteBanqueId:compte.compteBanqueDTO.id},function (result) {
                    if(result){
                        console.log(result);
                    }
                });
                reset();
            }, function (result) {
                console.log(result,'et');
            })

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

        function findValCpt() {
            console.log('vu 1');
            //vm.personneB.telephone =vm.personneB.telephone.toString();
          var val=  $("#field_typeFse").val() ;
            console.log('val',val);
            if(val ==='NON') {
                console.log('req',vm.personneB.telephone);
                console.log(vm.client);
                if(vm.datas===null) {
                    Client.getValeurCptClient({valeurCompte:vm.personneB.telephone}, function (response) {
                         if(response)
                         {
                             vm.refCompteB = vm.personneB.telephone;
                             console.log('ttt',response);
                         }
                    })
                }
                else {

                    Client.getValeurCptClient({clientId:vm.client.id}, function (result) {
                            var val =  result.numero;
                            console.log('spocke',result.numero);
                            console.log('to you',vm.comptes);
                            if(result.numero===0) {
                                console.log('ok.',result);
                                vm.refCompteB = vm.personneB.telephone;
                            } else {
                                console.log('oui',vm.num);

                                vm.refCompteB =vm.num+'-'+val;
                            }

                    });
                }

            }
            //vm.personneB.telephone= parseInt(vm.personneB.telephone);
        }

        vm.datePickerOpenStatus.dateOperation = false;

        function openCalendar(date) {
            console.log("open");
            vm.datePickerOpenStatus[date] = true;
        }

        vm.searchPersonne = function () {
            console.log("details des taxes2");
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
                    console.log("Beneficiaire",datas);
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
                }
            }, function () {

            });
        };

        vm.setAutotification = function setAutotification($file, personne) {
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
