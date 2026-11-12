(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('CreateCompteBanqueController', CreateCompteBanqueController);

    CreateCompteBanqueController.$inject = ['entity','ProfilActeur','GroupeProfilActeur','CancelAlert', 'ServiceProfil', '$scope', 'DataUtils', 'DateUtils', '$stateParams', '$uibModal','$uibModalInstance', 'Notification', 'Nationalite', 'RequestUtils', 'ParametreApp','UniteOrganisation'];

    function CreateCompteBanqueController(entity,ProfilActeur,GroupeProfilActeur,CancelAlert, ServiceProfil, $scope, DataUtils, DateUtils, $stateParams, $uibModal,$uibModalInstance, Notification, Nationalite, RequestUtils, ParametreApp,UniteOrganisation) {
        var vm = this;
        vm.client=entity;
        vm.clear = clear;
        vm.test=test;
        vm.verifeCompte=verifeCompte;
        vm.isSaving = false;
        vm.isModeCreate = true;
        vm.testIsCreate=false;
        vm.indexActuel = null;

        vm.compteBanque = {
            codeFse: null,
            rib:null,
            valeurCompte: null,
            codeBanque: null,
            codeAgence: null,
            clientId:null
        };

        vm.compteCarte = {
            codeFse: null,
            dateExpired:null,
            last4Digit: null,
            nomOnCard: null,
            clientId:null
        };
        console.log('----------',vm.client);



        RequestUtils.GetOrganisationByType.get({
            type: "PAYS"
        }, function (datas) {
            vm.pays = datas;
            console.log('turn', vm.pays)
        });




        vm.isEmpty = function () {
            return vm.comptes.length === 0;
        };


        vm.getFournisseurMonnaie=function(idpays) {
            console.log('Pays Id',idpays);
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
                        vm.fse=result.codeFse;
                    })
                }else {
                    console.log("Aucun fournisseur de monnaie n'a été trouvé");
                }
            });

        };



        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save(compteBanque) {
            console.log("---------Create compte banque---------------",compteBanque);
                console.log(compteBanque);
            //verifeCompte(vm.compteBanque);
            RequestUtils.SaveCompteBanque.save(vm.compteBanque,function (result) {
                console.log(result);
                Notification.success("Opération effectuée avec succès");
                $uibModalInstance.dismiss('cancel');
            })
        }

        function test() {
            console.log('vm.fseCompteId',vm.fse);
            vm.compteBanque.codeFse=vm.fse;
            vm.compteBanque.clientId=vm.client.client.client.id;
            console.log('tttttttttttt',vm.compteBanque);
            var message="";
            message+="Confirmez les informations du titulaire du compte\n"+ "Nom : ";

            CancelAlert.confirm(function (arg) {

                save(vm.compteBanque);
            }, function (arg) {
                // body...
            },message, false);
           /* RequestUtils.SaveCompteBanque.save(vm.compteBanque,function (result) {
                console.log(result);
                Notification.success("Opération effectuée avec succès");
                $uibModalInstance.dismiss('cancel');
            }) */
        }

         function verifeCompte() {
             vm.compteBanque.fseCompteId=vm.fseCompteId;
             vm.compteBanque.clientId=vm.client.client.client.id;
             vm.compteBanque.codeFse=vm.fse;
                RequestUtils.InfoClient.get({
                    compte: vm.compteBanque.valeurCompte,
                    agence: vm.compteBanque.codeAgence,
                    cle: vm.compteBanque.rib,
                    codeFse: vm.compteBanque.codeFse
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
                        save(vm.compteBanque);
                    }, function (arg) {
                        // body...
                    },message, false);


                }, function () {
                    Notification.error("COMPTE INTROUVABLE");
                });
        }



        vm.reset = reset;

        function reset() {

        }


        vm.getUoFils = function getUoFils(id, type) {
            console.log("recherche infos",id);
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
            console.log('Uo fils',vm.fse);
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
