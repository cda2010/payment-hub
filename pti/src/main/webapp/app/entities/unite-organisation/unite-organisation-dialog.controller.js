(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UniteOrganisationDialogController', UniteOrganisationDialogController);

    UniteOrganisationDialogController.$inject = ['Ville', 'PieceByTitulaireId', 'RequestUtils', '$timeout', 'Nationalite', 'TypePiece', 'PersonnePhysique', '$q', 'PersonneMorale', 'isViewing', 'isEdit', '$scope', 'DateUtils', '$uibModalInstance', 'entity', 'UniteOrganisation', 'TableauFrais', 'Compte', 'Autorisation', 'PointVente', 'TypeUniteOrganisation', 'Personne', 'GroupeProfilActeur','ServiceProfil','Commerciale'];

    function UniteOrganisationDialogController(Ville, PieceByTitulaireId, RequestUtils, $timeout, Nationalite, TypePiece, PersonnePhysique, $q, PersonneMorale, isViewing, isEdit, $scope, DateUtils, $uibModalInstance, entity, UniteOrganisation, TableauFrais, Compte, Autorisation, PointVente, TypeUniteOrganisation, Personne, GroupeProfilActeur,ServiceProfil,Commerciale) {
        var vm = this;

        vm.uniteOrganisation = entity;
        console.log('vm.uniteO-updated',vm.uniteOrganisation);
        vm.ok=[];
        vm.typepieces = TypePiece.query();
        vm.nationalites = Nationalite.query();
        vm.test12=[];
        vm.typeuniteorganisationsM= [];
        vm.typeuniteorganisationsSU=[];
        vm.typeuniteorganisationsMS=[];
        vm.typeuniteorganisationsUFU=[];
        vm.typeuniteorganisationsAU=[];
        vm.typeuniteorganisationsPU=[];
        vm.typeuniteorganisationsMM=[];
        vm.typeuniteorganisationsAgence=[];
        vm.typeuniteorganisationsFse=[];
        vm.typeuniteorganisationsSousDist=[];
        vm.typeuniteorganisationsPays=[];
        vm.typeuniteorganisationsAll=[];
        vm.typeuniteorganisationsZone=[];
        vm.seviceProfiles=ServiceProfil.queryByType({type:'ORGANISATION'});
        vm.typeuniteorganisations = TypeUniteOrganisation.queryOptimisetUo(function (result) {
            vm.test12.push(result);
            console.log('isEdit',isEdit);
            angular.forEach(result, function (value, index) {
                var v = {};
                vm.value=value;
                if((value.codeTypeUO==='MASTER' || value.codeTypeUO==='MARCHAND' || value.codeTypeUO==='UNITE_REGROUPEMENT' ) && value.codeTypeUO!=='FSE'){
                    vm.typeuniteorganisationsM.push(value);
                }
                 if(value.codeTypeUO==='SOUS_DISTRIBUTEUR' || value.codeTypeUO==='UNITE_REGROUPEMENT'){
                    vm.typeuniteorganisationsSU.push(value);
                }
                 if(value.codeTypeUO==='MARCHAND' || value.codeTypeUO==='SOUS_DISTRIBUTEUR' ){
                    vm.typeuniteorganisationsMS=null;
                } if(value.codeTypeUO==='UNITE_REGROUPEMENT' || value.codeTypeUO==='FSE'){
                    vm.typeuniteorganisationsUFU.push(value);
                }  if(value.codeTypeUO==='AGENCE' || value.codeTypeUO==='UNITE_REGROUPEMENT'){
                    vm.typeuniteorganisationsAU.push(value);
                }  if(value.codeTypeUO==='PAYS' || value.codeTypeUO==='UNITE_REGROUPEMENT'){
                    vm.typeuniteorganisationsPU.push(value);
                }  if(value.codeTypeUO==='MASTER' || value.codeTypeUO==='MARCHAND' ){
                    vm.typeuniteorganisationsMM.push(value);
                }   if(value.codeTypeUO==='FSE' || value.codeTypeUO==='UNITE_REGROUPEMENT'){
                    vm.typeuniteorganisationsFse.push(value);
                }  if(value.codeTypeUO==='SOUS_DISTRIBUTEUR'){
                    vm.typeuniteorganisationsSousDist.push(value);
                }  if(value.codeTypeUO==='PAYS' ){
                    vm.typeuniteorganisationsPays.push(value);
                }  if(value.codeTypeUO==='ZONE' ){
                    vm.typeuniteorganisationsZone.push(value);
                }

                 vm.typeuniteorganisationsAll.push(value);


            });
        });
        vm.typeuniteorganisationsNew =[];
        vm.commerciaux=Commerciale.query();
        vm.pereTypeUO=null;
        vm.typeAgence=null;
        vm.isAgent=false;
        vm.estLocalite=false;

        if(vm.uniteOrganisation.pereUOId){
            UniteOrganisation.get({id:vm.uniteOrganisation.pereUOId},function (result) {
                vm.pereTypeUO=result.typeUOCode;
                vm.typeAgence=result.typeUOLibelleTypeUO;
               if(result.typeUOLibelleTypeUO==='AGENCE'){
                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsM;

               } else if(result.typeUOLibelleTypeUO==='MASTER'){
                   console.log("yes");
                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsSU;
                 /*  angular.forEach(vm.typeuniteorganisations, function (value, index) {
                       var v = {};
                       if(value.codeTypeUO==='SOUS_DISTRIBUTEUR' || value.codeTypeUO==='UNITE_REGROUPEMENT'){
                           vm.typeuniteorganisationsNew.push(value);
                       }

                   });*/
               } else if(result.typeUOLibelleTypeUO==='MARCHAND'||result.typeUOLibelleTypeUO==='SOUS_DISTRIBUTEUR' ){
                   vm.typeuniteorganisationsNew=null;
               } else if(result.typeUOLibelleTypeUO==='PAYS'){
                   console.log('ok---*',vm.typeuniteorganisationsUFU);
                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsUFU;

               } else if(result.typeUOLibelleTypeUO==='FSE'){

                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsAU;

               } else if(result.typeUOLibelleTypeUO==='ZONE'){
                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsPU;

               } else if(result.typeUOLibelleTypeUO==='UNITE_REGROUPEMENT' ){
                   console.log('entrer');
                   if(result.pereUOId){
                       console.log('entrer 10.00');
                   UniteOrganisation.get({id:result.pereUOId},function (reponse){
                       vm.uniteOrganisation.estUniteRegroupement=true;
                       if(reponse.typeUOLibelleTypeUO==='FSE'){
                           vm.typeuniteorganisationsNew=vm.typeuniteorganisationsAU;
                            console.log('regggg',vm.typeuniteorganisationsAU);
                       }
                       if(reponse.typeUOLibelleTypeUO==='PAYS'){
                           vm.typeuniteorganisationsNew=vm.typeuniteorganisationsFse;

                       }
                       if(reponse.typeUOLibelleTypeUO==='AGENCE'){
                           console.log('ag',vm.typeuniteorganisationsM);
                           vm.typeuniteorganisationsNew=vm.typeuniteorganisationsM;

                       }
                       if(reponse.typeUOLibelleTypeUO==='MASTER'){
                           vm.typeuniteorganisationsNew=vm.typeuniteorganisationsSousDist;

                       }
                       if(reponse.typeUOLibelleTypeUO==='ZONE'){
                           vm.typeuniteorganisationsNew=vm.typeuniteorganisationsPays;

                       } else {
                           console.log('ok');
                           TypeUniteOrganisation.queryOptimisetUo(function (result){
                               vm.typeuniteorganisationsNew=vm.typeuniteorganisationsM;
                               console.log('bien',result);
                               angular.forEach(result, function (value, index){
                                   if(value.codeTypeUO==='AGENCE'){
                                       vm.typeuniteorganisationsNew.push(value);
                                       console.log('valide');
                                   }
                               });

                           });

                       }

                   });
                   }
                   else {
                       vm.typeuniteorganisationsNew=vm.typeuniteorganisationsZone;
                   }
               }
               else {
                   vm.typeuniteorganisationsNew=vm.typeuniteorganisationsAll;
               }
                vm.pereTypeUOChange();
            });
            console.log('typeAgence 2',vm.typeAgence);
        }
       console.log(vm.pereTypeUO);
        Ville.query({
            uoId: entity.pereUOId
        }, function (datas) {
            vm.villes = datas;
        });
        vm.openCalendar = openCalendar;
        vm.datePickerOpenStatus = {};
        vm.isViewing = isViewing;
        vm.isEdit = isEdit;
        vm.clear = clear;
        vm.save = save;
        //vm.uniteorganisations = UniteOrganisation.query();
        vm.tableaufrais = TableauFrais.query();
        vm.autorisations = Autorisation.query();
        vm.pointventes = PointVente.query();
        vm.groupeProfils=GroupeProfilActeur.queryByType({acteur:'ORGANISATION'});
        if(vm.uniteOrganisation.id!=null) {
            if(vm.uniteOrganisation.typeUOLibelleTypeUO==='PAYS' || vm.uniteOrganisation.typeUOLibelleTypeUO==='ZONE' || vm.uniteOrganisation.typeUOLibelleTypeUO==='UNITE_REGROUPEMENT') {
                vm.uniteOrganisation.rep=false;
            }else {
                vm.uniteOrganisation.rep=true;
            }
        }

        if (vm.uniteOrganisation.dateCreation)
            vm.uniteOrganisation.dateCreation = DateUtils.convertLocalDateFromServer(vm.uniteOrganisation.dateCreation);


        RequestUtils.GetFournisseur.query({}, function (datas) {
            vm.fournisseurservices = datas;
        });
        vm.typeUOChange = typeUOChange;
        vm.niveau = null;

        function typeUOChange() {
            TypeUniteOrganisation.getOptimise({
                id: vm.uniteOrganisation.typeUOId
            }, function (data) {
                vm.niveau = data.niveau;
                console.log('show',data);
                if(data.libelleTypeUO==='PAYS' || data.libelleTypeUO==='ZONE' || data.libelleTypeUO==='UNITE_REGROUPEMENT') {
                    vm.uniteOrganisation.rep=false;
                    console.log('Yes appartener');
                }else {
                    vm.uniteOrganisation.rep=true;
                    vm.isAgent=false;
                }
            }, function () {});
        }
       vm.pereTypeUOChange= function pereTypeUOChange() {
           console.log(vm.pereTypeUO);
            UniteOrganisation.queryByType({typeId:vm.pereTypeUO},function (result) {
                //console.log(result);

                vm.uniteorganisations=result;
            })
        };
        console.log(vm.uniteOrganisations,'et',vm.typeAgence);



        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        if (vm.isEdit || vm.isViewing) {
            console.log('update');
            TypeUniteOrganisation.getOptimise({
                id: vm.uniteOrganisation.typeUOId
            }, function (data) {
                vm.niveau = data.niveau;
                console.log('update TypeUo',data);
            }, function () {});
            if (vm.uniteOrganisation.representantId != null) {
                vm.uniteOrganisation.rep = true;
                Personne.get({
                    id: vm.uniteOrganisation.representantId
                }, function (data) {
                    vm.personneE = data;
                    if (vm.personneE.typePersonne == "PHYSIQUE") {
                        PersonnePhysique.get({
                            id: data.personnePhysiqueId
                        }, function (data) {
                            vm.personnePhysiqueE = data;
                        }, function () {});
                    }
                    if (vm.personneE.typePersonne == "MORALE") {
                        PersonneMorale.get({
                            id: data.personneMoraleId
                        }, function (data) {

                            if(data.dateRCCM!=null){
                                data.dateRCCM=DateUtils.convertLocalDateFromServer(data.dateRCCM);
                            }

                            vm.personneMoraleE = data;
                        }, function () {});
                    }
                    console.log('personne',data);
                    PieceByTitulaireId.queryByTitulaireId({
                        id: data.id
                    }, function (data) {
                        vm.pieceE = data;
                        vm.pieceE.dateDelivrance=DateUtils.convertLocalDateFromServer(vm.pieceE.dateDelivrance);
                        vm.pieceE.dateExpiration=DateUtils.convertLocalDateFromServer(vm.pieceE.dateExpiration);
                        console.log('piece',data);
                    }, function () {});
                }, function () {});
            }

        }



        function save() {
            console.log ("all "+vm.uniteOrganisation.rep);
            console.log ("organisation "+vm.uniteOrganisation.createdDate);
            vm.isSaving = true;
            if (vm.uniteOrganisation.rep) {
                var emetteur = {};
                emetteur.personneDTO = vm.personneE;
                if (vm.personneE.typePersonne == "PHYSIQUE") {
                    emetteur.personnePhysiqueDTO = vm.personnePhysiqueE;
                } else {
                    emetteur.personneMoraleDTO = vm.personneMoraleE;
                }
                emetteur.pieceDTO = vm.pieceE;
                vm.uniteOrganisation.personneAllDTO = emetteur;
            } else {
                vm.uniteOrganisation.personneAllDTO = null;
            }
            if (vm.uniteOrganisation.codeFse != null) {
                var fse = _.find(vm.fournisseurservices, {
                    'codeFse': vm.uniteOrganisation.codeFse
                });
                vm.uniteOrganisation.codeBanque = fse.codeExterne;
            }
            
            console.log ("service id "+vm.uniteOrganisation.serviceProfilId);

            if (vm.uniteOrganisation.id !== null) {
                console.log("organisa---"+vm.uniteOrganisation.id);
                console.log("organisa---"+vm.personneE);
                
                UniteOrganisation.update(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            } else {

                console.log ("service id "+vm.uniteOrganisation.serviceProfilId);
                UniteOrganisation.save(vm.uniteOrganisation, onSaveSuccess, onSaveError);
            }
        }


        function onSaveSuccess(result) {
            //$scope.$emit('ptiApp:uniteOrganisationUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }
        vm.datePickerOpenStatus.dateCreation = false;

        function openCalendar(date) {
            vm.datePickerOpenStatus[date] = true;
        }



    }
})();
