(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('UserManagementDialogController',UserManagementDialogController);

    UserManagementDialogController.$inject = ['Principal','UniteOrganisation','isViewing','isEdit','UniteOrganisationByType','RequestUtils','TypeUOWithoutUR','$stateParams', '$uibModalInstance', 'entity', 'User', 'TypeUniteOrganisation','$scope'];

    function UserManagementDialogController (Principal,UniteOrganisation,isViewing,isEdit,UniteOrganisationByType,RequestUtils,TypeUOWithoutUR,$stateParams, $uibModalInstance, entity, User, TypeUniteOrganisation,$scope) {
        var vm = this;

        vm.clear = clear;
        vm.isViewing = isViewing;
        vm.isEdit = isEdit;
        vm.languages = null;
        vm.typeUOId = entity.typeUOId;
        vm.profils = null;
        vm.uniteOrganisations = null;
        vm.loadSimpleAdminData = loadSimpleAdminData;
        vm.load = load;
        vm.save = save;
        vm.isSimpleUser = false;
        vm.isDis = false;
        vm.showPays = true;
        vm.showFse = true;
        vm.showAgence = true;
        vm.showMaster = true;
        vm.user = entity;
        console.log(entity);
        vm.fromUO=entity.fromUO;
        vm.organisationFSE = [];
        vm.organisationMaster = [];
         vm.organisationMarchand = [];
        vm.organisationAgence = [];
        vm.organisationSousDistributeur = [];
        vm.authorities = [];
        vm.userRole=User.getByLogin({login : vm.user.login},function (result) {
            vm.user.authorities=result.authorities;
        });

        if(vm.fromUO){
            console.log('init',vm.fromUO);
            load()
        }
        vm.verifLenght=function(login){
            if(login!=null){
                return login.length<6;
            }
        };
        TypeUOWithoutUR.optimise({}, function (datas) {
            //removeItem(datas,"libelleTypeUO","FSE");
            console.log('type with out',datas);
            vm.typeUniteOrganisations = datas;
        });

        RequestUtils.GetOrganisationByTypeOptimise.get({ type: "PAYS" }, function(datas) {
            vm.organisationPays = datas;
        });

        Principal.identity().then(function(account) {
            vm.account = account;
            vm.authorities = account.authorities;
            if(vm.isViewing){
                vm.isDis = true;
            }
            if(findObjectByKey(vm.authorities,"ROLE_ADMIN_SYSTEME")==null && vm.isEdit){
                vm.isDis = true;
            }
        });

        function loadSimpleAdminData() {
            if(findObjectByKey(vm.authorities,"ROLE_ADMIN_SYSTEME")==null) {
                vm.showPays =false;
                vm.showFse =false;
                if(!vm.isEdit){
                    initUO(vm.account.uniteOrganisationId);
                }
                console.log("entrer*********---******",vm.account.uniteOrganisationId);
                UniteOrganisation.queryByIdOptimise({id: vm.account.uniteOrganisationId}, function (datas) {
                    TypeUniteOrganisation.getOptimise({id :  datas.typeUOId}, function (datas) {
                        if(!vm.isEdit) {
                            vm.typeUOId = null;
                        }else{
                            UniteOrganisation.queryByIdOptimise({id:vm.user.uniteOrganisationId}, function (datas) {
                                vm.typeUOId = datas.typeUOId;
                                TypeUniteOrganisation.getOptimise({id : vm.typeUOId}, function (datas) {
                                    vm.typeActeur = datas.libelleTypeUO;
                                });
                            });
                        }
                        if(datas.libelleTypeUO=="AGENCE"){
                            vm.showAgence = false;
                            if(!vm.isEdit) {
                                vm.agenceId = vm.account.uniteOrganisationId;
                                vm.user.uniteOrganisationId = vm.account.uniteOrganisationId;
                            }
                        }
                        if(datas.libelleTypeUO=="MASTER"){
                            vm.showAgence = false;
                            vm.showMaster = false;
                            if(!vm.isEdit) {
                                vm.masterId = vm.account.uniteOrganisationId;
                                vm.user.uniteOrganisationId = vm.account.uniteOrganisationId;
                            }
                            removeItem(vm.typeUniteOrganisations,"libelleTypeUO","AGENCE");

                        }
                    });
                });
            }
        }



        vm.organisationColums = [{
            name: 'libelleUniteOrganisation',
            title: 'Libelle'
        }];

        $scope.$on("onLoadOrganisation", function() {
        });

        $scope.$watch("vm.paysId", function(id) {
            console.log('pays');
            vm.organisationFSE = getUoFils("FSE", id);
        });

        $scope.$watch("vm.fseId", function(id) {
            getUoFils("AGENCE", id);
        });

        $scope.$watch("vm.agenceId", function(id) {
            if (vm.typeActeur==='MASTER'){
                 getUoFils("MASTER", id);
            }
            if (vm.typeActeur==='MARCHAND'){
                console.log('marchand');
                 getUoFils("MARCHAND", id);
            }
            if (vm.typeActeur==='SOUS_DISTRIBUTEUR'){
                getUoFils("MASTER", id);
            }
        });

        $scope.$watch("vm.masterId", function(id) {
            getUoFils("SOUS_DISTRIBUTEUR", id);
        });

        function getUoFils(type, id) {
            if (id != "" && type != "" && id != null && type != null) {
                RequestUtils.GetUoFilsByType.get({
                    type: type,
                    uoId: id
                }, function(datas) {
                    console.log('result',datas);
                    switch (type) {
                        case "FSE":
                            vm.organisationFSE = datas;
                            console.log('FSE',vm.organisationFSE);
                            break;
                        case "AGENCE":
                            vm.organisationAgence = datas;
                            console.log('agence',vm.organisationAgence);
                            break;
                        case "MASTER":
                            vm.organisationMaster = datas;
                            console.log('master',vm.organisationMaster);
                            break;
                         case "MARCHAND":
                            // vm.organisatvm.organisationPaysionMaster
                            vm.organisationPaysionMaster = datas;
                             console.log('marchand',vm.organisationPaysionMaster);
                            break;
                        case "SOUS_DISTRIBUTEUR":
                            vm.organisationSousDistributeur = datas;
                            console.log('sous-distr',vm.organisationSousDistributeur);
                            break;
                    }
                    console.log("organisation pays");
                    console.log('data',datas);
                    return datas;
                });
            } else {
            }
        }

        function initUO(uoId) {
            RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"PAYS"}, function (datas) {
                console.log('Pays',datas);
                vm.paysId = datas.id;
            });
            
            RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"FSE"}, function (datas) {
                console.log('FSE',datas);
                vm.fseId = datas.id;
            });

            UniteOrganisation.queryByIdOptimise({id:uoId}, function (datas) {
                vm.typeUOId = datas.typeUOId;
                TypeUniteOrganisation.getOptimise({id : vm.typeUOId}, function (datas) {
                    vm.typeActeur = datas.libelleTypeUO;
                    console.log('type act',vm.typeActeur);
                    if(vm.typeActeur == 'MASTER'){
                        RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"AGENCE"}, function (datas) {
                            vm.agenceId = datas.id;
                            console.log('master',datas);
                        });
                    }
                    if(vm.typeActeur == 'SOUS_DISTRIBUTEUR'){
                        RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"AGENCE"}, function (datas) {
                            vm.agenceId = datas.id;
                            console.log('sous',datas);
                        });
                        RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"MASTER"}, function (datas) {
                            vm.masterId = datas.id;
                            console.log('master1',datas);
                        });
                    }
                    if(vm.typeActeur == 'MARCHAND'){
                        RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"AGENCE"}, function (datas) {
                            vm.agenceId = datas.id;
                            console.log('agence',datas);
                        });
                        RequestUtils.GetUoPereByTypeOptimise.get({uoId : uoId,libelleTypeUO :"MASTER"}, function (datas) {
                            vm.masterId = datas.id;
                            console.log('master2',datas);
                        });
                    }
                });
            });
        }


        if(vm.isEdit || vm.isViewing){
            initUO(vm.user.uniteOrganisationId);
        }

        function load(){
            console.log('ouiu',vm.typeUOId);
                if(angular.isDefined(vm.typeUOId)){
                    TypeUniteOrganisation.getOptimise({id : vm.typeUOId}, function (datas) {
                        vm.typeActeur = datas.libelleTypeUO;
                        console.log('type act',vm.typeActeur);
                    });
                    RequestUtils.ProfilsByTypeUOOptimise.query({typeId:vm.typeUOId}, function (datas) {
                        vm.profils = datas;
                    });
                    UniteOrganisationByType.queryOptimise({typeId:vm.typeUOId}, function (datas) {
                        vm.uniteOrganisations = datas;
                        vm.user = entity;
                    });
                }
        }

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function onSaveSuccess (result) {
            vm.isSaving = false;
            $uibModalInstance.close(result);
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        function save () {
            vm.user.langKey = "fr";
            vm.isSaving = true;
            if (vm.user.id !== null) {
                User.update(vm.user, onSaveSuccess, onSaveError);
            } else {
                User.save(vm.user, onSaveSuccess, onSaveError);
            }
        }

        if(vm.isEdit || vm.isViewing){
            UniteOrganisation.queryByIdOptimise({id:vm.user.uniteOrganisationId}, function (datas) {
                vm.typeUOId = datas.typeUOId;
                vm.load();
            });
        }

        //fonction pour supprimer un element d'un tableau
        var removeItem = function (object, key, value) {
            if (value == undefined)
                return;
            for (var i in object) {
                if (object[i][key] == value) {
                    object.splice(i, 1);
                }
            }
        };

        function findObjectByKey(array, value) {
            for (var i = 0; i < array.length; i++) {
                if (array[i] === value) {
                    return array[i];
                }
            }
            return null;
        }

    }
})();
